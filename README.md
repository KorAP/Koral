![Koral](https://raw.githubusercontent.com/KorAP/Koral/master/misc/koral.png)

Koral is a library designed for the translation of different corpus query 
languages to KoralQuery, a JSON-LD-based protocol for the common representation
of linguistic queries. This work has been carried out within the KorAP
project (see below) and forms the major part of a Master thesis that is
due to appear. The detailed specifications of KoralQuery will be covered
in that thesis.

As of v0.1, the following corpus query languages (QLs) are supported:
* [Cosmas-II QL](http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/) 
* [ANNIS QL](http://annis-tools.org/aql.html)
* [Poliqarp QL](http://korpus.pl/en/cheatsheet/node3.html) (extended by numerous operators to "PoliqarpPlus" QL)
* [CQL](http://www.loc.gov/standards/sru/cql/spec.html)

You can use the main class QuerySerializer to translate and serialize queries
for you. The usage example below illustrates this. Valid QL identifiers
are `cosmas2`, `annis`, `poliqarp`, `poliqarpplus` and `cql`.


## Usage Example


```java
import de.ids_mannheim.korap.query.serialize.QuerySerialzer;
QuerySerializer qs = new QuerySerializer();
String query = "contains(<s>,[orth=zu][pos=ADJA])";
qs.setQuery(query, "poliqarpplus");
System.out.println(qs.toJSON());
```

This will print out the following JSON-LD string for the Koralized query.
The query asks for a sentence element (`<s>`) that is contained in a
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

Koral enables the design and implementation of corpus query systems 
independently of any specific query languages. All the system needs to do on
the query processing side is have the query translated to KoralQuery (see usage)
and feed the translated query to its search engine. In particular, several query
 languages can be supported without further adjustments to the search engine.

Koral and KoralQuery have been designed and developed within the 
[KorAP Project](http://korap.ids-mannheim.de/), and are used in KorAP to 
translate queries to a common format before sending them to the backend.

## Installation

Installation is straightforward (Maven3 required):

    git clone https://github.com/korap/Koral [install-dir]
    cd [install-dir]
    mvn test
    mvn install

There is also a command line version. After installation, simply run

    java -jar target/Koral-0.2.jar [query] [queryLanguage]
    
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
See also the attached LICENSE.

The [ANNIS grammar](https://github.com/korpling/ANNIS/tree/develop/annis-service/src/main/antlr4/annis/ql) is licensed under the Apache License 2.0.
