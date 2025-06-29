package local.billy.evaluation.documents_batch_process.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

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
@JacksonXmlRootElement(localName = "Documento")
public class Document {
    
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String id;

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "Cliente")
    private String clientName;
    
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "Tipo")
    private String type;
    
    @JsonProperty("totalAPagar")
    @JacksonXmlProperty(localName = "TotalAPagar")
    private float amount;

    @JsonProperty("medioPago")
    @JacksonXmlProperty(localName = "MedioPago")
    private String payMethod;

}
