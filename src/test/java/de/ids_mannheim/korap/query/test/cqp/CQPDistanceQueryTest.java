package de.ids_mannheim.korap.query.test.cqp;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.ids_mannheim.korap.query.test.BaseQueryTest;

public class CQPDistanceQueryTest extends BaseQueryTest {

    public CQPDistanceQueryTest () {
        super("CQP");
    }

    @Test
    public void testDistanceBeyondSentenceBoundary ()
            throws JsonMappingException, JsonProcessingException {
        JsonNode n = runQuery("\"copil\" []{,5} \"cuminte\" <base/s=s>");
//        System.out.println(n.toPrettyString());
        
        // EM: I think the query should be serialized as follows:
        /*
{
    "query": {
        "operands": [
            {
                "@type": "koral:token",
                "wrap": {
                    "@type": "koral:term",
                    "match": "match:eq",
                    "type": "type:regex",
                    "layer": "orth",
                    "key": "copil"
                }
            },
            {
                "operands": [
                    {"@type": "koral:token"}
                ],
                "boundary": {
                    "min": 0,
                    "max": 5,
                    "@type": "koral:boundary"
                },
                "@type": "koral:group",
                "operation": "operation:repetition"
            },
            {
                "frames": [
                    "frames:endsWith",
                    "frames:matches"
                ],
                "@type": "koral:group",
                "operation": "operation:position",
                "operands": [
                    {
                        "@type": "koral:span",
                        "wrap": {
                            "foundry": "base",
                            "@type": "koral:term",
                            "layer": "s",
                            "key": "s"
                        }
                    },
                    {
                        "@type": "koral:token",
                        "wrap": {
                            "@type": "koral:term",
                            "match": "match:eq",
                            "type": "type:regex",
                            "layer": "orth",
                            "key": "cuminte"
                        }
                    }
                ]
            }
        ],
        "@type": "koral:group",
        "operation": "operation:sequence"
    },
    "@context": "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld"
}
         */
       
    }

    
}
