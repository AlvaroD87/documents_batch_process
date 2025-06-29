package local.billy.evaluation.documents_batch_process.camel_routes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64.Decoder;
import java.util.zip.GZIPInputStream;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.billy.evaluation.documents_batch_process.models.Document;
import local.billy.evaluation.documents_batch_process.models.FileProcess;
import local.billy.evaluation.documents_batch_process.models.MetaDocument;

@Component
public class DocumentProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentProcessor.class);

    //@Autowired
    private ObjectMapper objectMapper;
    
    //@Autowired
    private Decoder decoder;

    @Value("${folder.output.files}")
    private String folderOutputFiles;

    public DocumentProcessor(ObjectMapper objectMapper, Decoder decoder){
		this.objectMapper = objectMapper;
        this.decoder = decoder;
	}

    
    public void readBase64Document(Exchange exchange) throws IOException{

        MetaDocument metaDocument = objectMapper.readValue(exchange.getIn().getBody(String.class), MetaDocument.class);
    
        byte[] decodedBytes = decoder.decode(metaDocument.getDocBase64());

        ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
        GZIPInputStream gzis = new GZIPInputStream(bais);
        byte[] decompressedBytes = gzis.readAllBytes();
        gzis.close();
        bais.close();

        String decompressedDocument = new String(decompressedBytes, StandardCharsets.UTF_8);

        metaDocument.setDocument(objectMapper.readValue(decompressedDocument, Document.class));
        
        exchange.getIn().setBody(metaDocument);

        LOG.info(String.format("Descomprimido Documento de %s %s", metaDocument.getDocType(), metaDocument.getInternNDoc()));
    }

    public void getXmlDocument(Exchange exchange) throws IOException{

        MetaDocument metaDocument = exchange.getIn().getBody(MetaDocument.class);

        String fileName = String.format("%s_%s.xml", metaDocument.getDocType(), metaDocument.getInternNDoc());

        String strPathFilesXml = String.format("%s/%s", folderOutputFiles, exchange.getProperty("fileProcess", FileProcess.class).getFolderOutputFiles());

        Path xmlPath = Paths.get(strPathFilesXml, fileName);

        Files.createDirectories(Paths.get(strPathFilesXml));

        Files.writeString(
                xmlPath,
                String.format(
                    """
                <?xml version"1.0" encoding="ISO-8859-1" standalone="no"?>
                <DTE version="1.0">
                    <Documento ID="%s">
                        <Cliente>%s</Cliente>
                        <Tipo>%s</Tipo>
                        <TotalAPagar>%.2f</TotalAPagar>
                        <MedioPago>%s</MedioPago>
                    </Documento>
                </DTE>
                </xml>
                    """
                , metaDocument.getDocument().getId()
                , metaDocument.getDocument().getClientName()
                , metaDocument.getDocument().getType()
                , metaDocument.getDocument().getAmount()
                , metaDocument.getDocument().getPayMethod()
            ) 
        );

        exchange.getIn().setBody(metaDocument.getDocument());

        LOG.info(String.format("Generado archivo xml %s", fileName));
    }

    public void uploadFileProcess(Exchange exchange){
        
        Document document = exchange.getIn().getBody(Document.class);

        exchange.getProperty("fileProcess", FileProcess.class).addPayMethodData(document.getPayMethod(), document.getAmount());
        exchange.getProperty("fileProcess", FileProcess.class).addDocumentProcessed();
        
        LOG.info(String.format("Actualización de documentos procesados", exchange.getProperty("fileProcess", FileProcess.class).getTotalDocumentsProcessed()));
    }

}
