package local.billy.evaluation.documents_batch_process.camel_routes;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import local.billy.evaluation.documents_batch_process.models.FileProcess;

@Component
public class CamelContextRoutes extends RouteBuilder{

    @Value("${folder.output.files}")
    private String folderOutputFiles;

    @Override
    public void configure() throws Exception {
        
        from("file:{{folder.batch.documents.file}}?move=done&include=.*.json")
            .routeId("fileBatchDocumentsProcess")
            .unmarshal().json(JsonLibrary.Jackson, JsonNode.class)
            .process(exchange -> {
                JsonNode jsonFile = exchange.getIn().getBody(JsonNode.class);

                exchange
                    .getIn()
                    .setBody(
                        jsonFile.has("documents") && jsonFile.get("documents").isArray() 
                            ? jsonFile.get("documents")
                            : ""
                    );
                
                exchange.setProperty(
                    "fileProcess", 
                    FileProcess
                    .builder()
                        .fileName(exchange.getIn().getHeader(Exchange.FILE_NAME, String.class))
                        .iniProcess(LocalDateTime.now())
                        .totalDocuments(jsonFile.get("documents").isArray() ? ((ArrayNode)jsonFile.get("documents")).size() : 0)
                        .totalDocumentsProcessed(new AtomicInteger(0))
                        .columnsForReport(new ConcurrentHashMap<String, ConcurrentHashMap<String, Float>>())
                    .build()
                );
            })
            .choice()
                .when(simple("${body.isArray()}"))
                    .split(body())
                        .parallelProcessing()
                        .to("bean:documentProcessor?method=readBase64Document")
                        .to("bean:documentProcessor?method=getXmlDocument")
                        .to("bean:documentProcessor?method=uploadFileProcess")
                    .end()
            .end()
            .process(exchange -> {

                FileProcess fileProcess = exchange.getProperty("fileProcess", FileProcess.class);

                fileProcess.setEndProcess(LocalDateTime.now());

                fileProcess.generateHtmlReport(String.format("%s/%s", folderOutputFiles, fileProcess.getFolderOutputFiles()));

                fileProcess.generateProcessReport(String.format("%s/%s", folderOutputFiles, fileProcess.getFolderOutputFiles()));
            })
            ;
            
    }

}
