## Koral v1.0

Koral is a library designed for the translation of different corpus query 
languages to KoralQuery, a JSON-LD-based protocol for the representation
of linguistic queries. 

As of v1.0, the following corpus query languages (QLs) are supported:
* [Cosmas-II QL][http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/] 
* [ANNIS QL][http://annis-tools.org/aql.html]
* [Poliqarp QL][http://korpus.pl/en/cheatsheet/node3.html] (extended by numerous operators to "PoliqarpPlus" QL)
* [CQP][http://www.loc.gov/standards/sru/cql/spec.html]

## Code Example

You can use the main class QuerySerializer to translate and serialize queries
for you. The following code snippet illustrates this. Valid QL identifiers
are `cosmas', `annis', `poliqarp', `poliqarpplus' and `cqp'.

```java
import de.ids_mannheim.korap.query.serialize.QuerySerialzer;
QuerySerializer qs = new QuerySerializer();
qs.setQuery("This is a poliqarp query.", "poliqarp");
System.out.println(qs.toJSON());
```

This will print out a JSON-LD string with you Koralized query. 
There is also a command line version. After installation, simply run

```
java -jar target/Koral-1.0.jar [query] [queryLanguage]
'''

## Motivation

Koral and KoralQuery have been designed and developed within the [KorAP Project][http://korap.ids-mannheim.de/].
Through Koral, linguists can use the KorAP query engine with the QL of their 
preference. As the KorAP backend only sees the incoming KoralQuery,
new QLs can be supported by KorAP without having to change a single line of
code in the backend.

## Installation

Installation is straightforward:

```
git clone https://github.com/korap/Koral [install-dir]
cd [install-dir]
mvn install
'''

## License

Koral is published under the Perl [Artistic License][http://opensource.org/licenses/artistic-license-2.0].
See also the attached LICENSE.
