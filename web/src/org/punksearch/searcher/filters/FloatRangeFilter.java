package org.punksearch.searcher.filters;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Filter;

public class FloatRangeFilter extends Filter {

	private String fieldName;
	private Float lowerTerm = 0.0f;
	private Float upperTerm = 0.0f;
	private boolean includeLower;
	private boolean includeUpper;	
	
	public FloatRangeFilter(String fieldName, Float lowerTerm, Float upperTerm, boolean includeLower, boolean includeUpper) {
		this.fieldName = fieldName;
		this.lowerTerm = lowerTerm;
		this.upperTerm = upperTerm;
		this.includeLower = includeLower;
		this.includeUpper = includeUpper;

		if (null == lowerTerm && null == upperTerm) {
			throw new IllegalArgumentException("At least one value must be non-null");
		}
		if (includeLower && null == lowerTerm) {
			throw new IllegalArgumentException("The lower bound must be non-null to be inclusive");
		}
		if (includeUpper && null == upperTerm) {
			throw new IllegalArgumentException("The upper bound must be non-null to be inclusive");
		}
	}
	
	public BitSet bits(IndexReader reader) throws IOException {
        BitSet bits = new BitSet(reader.maxDoc());
        TermEnum enumerator = reader.terms(new Term(fieldName, ""));
        
        try {
            
            if (enumerator.term() == null) {
                return bits;
            }
        
            TermDocs termDocs = reader.termDocs();
            try {
                do {
                    Term term = enumerator.term();
                    if (term != null && term.field().equals(fieldName)) {
                    	Float termFloat = Float.valueOf(term.text());
                        if (lowerTerm == null || lowerTerm <  termFloat || (includeLower && lowerTerm == termFloat)) {
                            if (upperTerm != null && (upperTerm < termFloat || (!includeUpper && upperTerm == termFloat))) {
                                continue;
                            }
                            // we have a good term, find the docs
                            termDocs.seek(enumerator.term());
                            while (termDocs.next()) {
                                bits.set(termDocs.doc());
                            }
                            
                        }
                    } else {
                        break;
                    }
                }
                while (enumerator.next());
                
            } finally {
                termDocs.close();
            }
        } finally {
            enumerator.close();
        }
        
		System.out.println(lowerTerm + " " + upperTerm);

        return bits;
	}

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(fieldName);
        buffer.append(":");
        buffer.append(includeLower ? "[" : "{");
        if (null != lowerTerm) {
            buffer.append(lowerTerm);
        }
        buffer.append("-");
        if (null != upperTerm) {
            buffer.append(upperTerm);
        }
        buffer.append(includeUpper ? "]" : "}");
        return buffer.toString();
    }

    /** Returns true if <code>o</code> is equal to this. */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FloatRangeFilter)) return false;
        FloatRangeFilter other = (FloatRangeFilter) o;

        if (!this.fieldName.equals(other.fieldName)
            || this.includeLower != other.includeLower
            || this.includeUpper != other.includeUpper
           ) { return false; }
        if (this.lowerTerm != null ? !this.lowerTerm.equals(other.lowerTerm) : other.lowerTerm != null) return false;
        if (this.upperTerm != null ? !this.upperTerm.equals(other.upperTerm) : other.upperTerm != null) return false;
        return true;
    }	
	
}
