![Koral](https://raw.githubusercontent.com/KorAP/Koral/master/misc/koral.png)

Koral is a translator tool for converting different corpus query 
languages to [KoralQuery](https://korap.github.io/Koral/), a JSON-LD-based protocol for the common representation
of linguistic queries. KoralQuery specifications are described extensively in Bingel (2015). This work has been carried out within the KorAP project. 

Koral supports the following corpus query languages (QLs):
* [Cosmas-II QL](http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/) 
* [ANNIS QL](http://annis-tools.org/aql.html)
* [Poliqarp QL](http://korpus.pl/en/cheatsheet/node3.html) (extended by numerous operators to "PoliqarpPlus" QL)
* [CQL](http://www.loc.gov/standards/sru/cql/spec.html) (for basic search as described in [the CLARIN FCS 1.0 Specification](https://www.clarin.eu/content/federated-content-search-clarin-fcs) )
* FCSQL (based on [CQP](http://cwb.sourceforge.net/files/CQP_Tutorial/), for advanced search as described in the CLARIN FCS 2.0 specification draft)

## Usage Example

You can use the main class QuerySerializer to translate and serialize queries. Valid QL identifiers are `cosmas2`, `annis`, `poliqarp`, `poliqarpplus`, `cql` and `fcsql`.

```java
import de.ids_mannheim.korap.query.serialize.QuerySerialzer;

QuerySerializer qs = new QuerySerializer();
String query = "contains(<s>,[orth=zu][pos=ADJA])";
qs.setQuery(query, "poliqarpplus");
System.out.println(qs.toJSON());
```

This will print out the following JSON-LD string for the Koralized query.
The query asks for a sentence element (`<s>`) contained in a
sequence of the surface form *zu* and a token with the part-of-speech tag *ADJA*.
In the KoralQuery string, a containment relation is defined over two
operands, an *s* span and a sequence of two tokens.

```json
{
  "@context": "http://korap.ids-mannheim.de/ns/KoralQuery/v0.2/context.jsonld",
  "query": {
    "@type": "koral:group",
    "operation": "operation:position",
    "frames": [
      "frames:isAround"
    ],
    "operands": [
      {
        "@type": "koral:span",
        "key": "s"
      },
      {
        "@type": "koral:group",
        "operation": "operation:sequence",
        "operands": [
          {
            "@type": "koral:token",
            "wrap": {
              "@type": "koral:term",
              "layer": "orth",
              "key": "zu",
              "match": "match:eq"
            }
          },
          {
            "@type": "koral:token",
            "wrap": {
              "@type": "koral:term",
              "layer": "pos",
              "key": "ADJA",
              "match": "match:eq"
            }
          }
        ]
      }
    ]
  }
}
```


## Motivation

Koral allows designing and implementating corpus query systems 
independent of any specific query languages. The systems only need to have Koral translate a query to a KoralQuery (see usage)
and feed the translated query to their search engine. Several query languages can be supported without further adjustments to the search engine.

Koral and KoralQuery have been designed and developed within the 
[KorAP Project](http://korap.ids-mannheim.de/), and are used in KorAP to 
translate queries to a common format before sending them to its search engine.

## Installation

Installation is straightforward (Maven3 required):

    git clone https://github.com/korap/Koral [install-dir]
    cd [install-dir]
    mvn test -Dhttps.protocols=TLSv1.2
    mvn install

There is also a command line version. After installation, simply run

    java -jar target/Koral-0.2.jar [query] [queryLanguage]
   
## Prerequisites

* Java 7 (OpenJDK or Oracle JDK with [JCE](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html))
* [Git](http://git-scm.com/)
* At least [Maven 3.2.1](https://maven.apache.org/)
* Further dependencies are resolved by Maven.

## Publications

J. Bingel, "Instantiation and implementation of a corpus query lingua franca," M.S. thesis, University of Heidelberg, Heidelberg, 2015. 

J. Bingel and N. Diewald, "KoralQuery – a General Corpus Query Protocol," in Proceedings of the Workshop on Innovative Corpus Query and Visualization Tools at NODALIDA 2015, Vilnius, 2015, pp. 1-5.

## Authorship

Koral and KoralQuery were developed by Joachim Bingel,
Nils Diewald, Michael Hanl and Eliza Margaretha at IDS Mannheim.

The ANTLR grammars for parsing ANNIS QL and COSMAS II QL were developed by 
Thomas Krause (HU Berlin) and Franck Bodmer (IDS Mannheim), respectively.
Minor adaptations of those grammars were implemented by the Koral authors.

The authors wish to thank Piotr Bański, Franck Bodmer, Elena Frick and 
Carsten Schnober for their valuable input.

## License

Koral is published under the BSD-2 License.
See also the attached [LICENSE](https://github.com/KorAP/Koral/blob/master/LICENSE).

The [ANNIS grammar](https://github.com/korpling/ANNIS/tree/develop/annis-service/src/main/antlr4/annis/ql) is licensed under the Apache License 2.0.
