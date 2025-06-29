package local.billy.evaluation.documents_batch_process.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaDocument {

    @JsonProperty("DocType")
    private String docType;

    @JsonProperty("NroDocInterno")
    private String internNDoc;

    @JsonProperty("ContentBase64")
    private String docBase64;

    @JsonProperty("Document")
    private Document document;
}
