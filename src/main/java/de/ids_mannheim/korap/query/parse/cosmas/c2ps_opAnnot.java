package de.ids_mannheim.korap.query.parse.cosmas;

/* COSMAS II Plain Syntax (c2ps).
 * lokale Grammatik für Optionen von #IN(Opts).
 * 12.12.12/FB
 *
 * strip(): MORPH(NP sg nom) -> NP sg nom.
 */

public class c2ps_opAnnot

{

    public static String strip (String input) {
        if (input.startsWith("MORPH(")) {
            input = input.substring(6, input.length() - 1);
        }

        return input;
    }


    /*
     * main: testprogram:
     */

    public static void main (String args[]) throws Exception {} // main

}
