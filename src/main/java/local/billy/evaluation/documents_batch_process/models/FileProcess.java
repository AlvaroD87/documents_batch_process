package local.billy.evaluation.documents_batch_process.models;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileProcess {

    private String fileName;
    private LocalDateTime iniProcess;
    private LocalDateTime endProcess;
    private int totalDocuments;
    //private int totalDocumentsProcessed;
    private AtomicInteger totalDocumentsProcessed; // = new AtomicInteger(0);
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Float>> columnsForReport;

    public String getFolderOutputFiles(){
        return String.format("%s_%s", fileName.replace(".json", ""), iniProcess.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
    }

    public void addDocumentProcessed(){
        totalDocumentsProcessed.incrementAndGet();
    }

    public void addPayMethodData(String payMethod, Float amountToPay){
        payMethod = payMethod.equals("") || payMethod == null? "Sin Metodo de Pago" : payMethod;
        if (!columnsForReport.containsKey(payMethod)){
            columnsForReport.put(payMethod, new ConcurrentHashMap<String, Float>());
            columnsForReport.get(payMethod).put("amountDocs", (float) 0.0);
            columnsForReport.get(payMethod).put("totalToPay", (float) 0.0);
        }

        columnsForReport.get(payMethod).compute("amountDocs", (k,d) -> d+=1);
        columnsForReport.get(payMethod).compute("totalToPay", (k,d) -> d+=amountToPay);
    }

    public void generateHtmlReport(String outputFilePath) {

        if (columnsForReport == null || columnsForReport.isEmpty()) {
            throw new IllegalStateException("No data available to generate report.");
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><title>Reporte</title></head><body>");
        html.append("<table border='1' cellpadding='5' cellspacing='0'>");

        // Header row
        html.append("<tr><th></th>");
        for (String medioPago : columnsForReport.keySet()) {
            html.append("<th>").append(medioPago).append("</th>");
        }
        html.append("<th>TOTALES</th></tr>");

        // Initialize totals
        float totalDocsSum = 0;
        float totalAmountSum = 0;

        // Row: Cantidad Docs
        html.append("<tr><td><b>Cantidad Docs</b></td>");
        for (String medioPago : columnsForReport.keySet()) {
            float amountDocs = columnsForReport.get(medioPago).getOrDefault("amountDocs", 0.0f);
            html.append("<td>").append(String.format("%.0f", amountDocs)).append("</td>");
            totalDocsSum += amountDocs;
        }
        html.append("<td><b>").append(String.format("%.0f", totalDocsSum)).append("</b></td></tr>");

        // Row: Total a Pagar
        html.append("<tr><td><b>Total a Pagar</b></td>");
        for (String medioPago : columnsForReport.keySet()) {
            float totalToPay = columnsForReport.get(medioPago).getOrDefault("totalToPay", 0.0f);
            html.append("<td>").append(String.format("%.2f", totalToPay)).append("</td>");
            totalAmountSum += totalToPay;
        }
        html.append("<td><b>").append(String.format("%.2f", totalAmountSum)).append("</b></td></tr>");

        html.append("</table>");
        html.append("</body></html>");

        try {

            Path xmlPath = Paths.get(outputFilePath, "report.html");

            Files.createDirectories(Paths.get(outputFilePath));

            Files.writeString(xmlPath,html.toString());
        
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error writing HTML report", e);
        }
    }

    public void generateProcessReport(String outputFilePath){
        
        Duration delta = Duration.between(iniProcess, endProcess);
        
        String jsonReport = String.format("""
                {
                    "Nombre de Archivo": "%s",
                    "Cantidad de documentos": %d,
                    "Cantidad de documentos procesados": %d,
                    "Datetime inicio de procesamiento": "%s",
                    "Datetime fin de procesamiento": "%s",
                    "Delta time de procesamiento": {
                        "Horas": %d,
                        "Minutos": %d,
                        "Segundos": %d,
                        "Milisegundos": %d
                    }
                }
            """
                ,fileName
                ,totalDocuments
                ,totalDocumentsProcessed.get()
                ,iniProcess.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                ,endProcess.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                ,delta.toHours()
                ,delta.toMinutes() % 60
                ,delta.getSeconds() % 60
                ,delta.getNano() / 1000000
        );

        try {

            Path jsonReportPath = Paths.get(outputFilePath, "processReport.json");

            Files.createDirectories(Paths.get(outputFilePath));

            Files.writeString(jsonReportPath,jsonReport);
        
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error writing Json report", e);
        }


    }
}
