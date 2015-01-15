package de.ids_mannheim.korap.query.serialize;

/**
 * This class serves as a dummy class for reporting errors when the
 * query or query language as specified in the
 * {@link #QuerySerializer} are empty or erroneous. Without
 * instatiating a class, errors/warnings cannot be reported.
 * 
 * @author bingel
 *
 */
public final class DummyQueryProcessor extends AbstractQueryProcessor {

    @Override
    public void process(String query) {
        // This is just a dummy class. Do nothing!
    }
}
