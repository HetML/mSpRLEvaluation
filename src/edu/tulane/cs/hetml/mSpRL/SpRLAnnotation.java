package edu.tulane.cs.hetml.mSpRL;

/**
 * Created by taher on 7/30/16.
 */
public interface SpRLAnnotation {
    Integer getStart();

    Integer getEnd();

    void setStart(Integer x);

    void setEnd(Integer x);

    String getText();

    void setText(String text);

    String getId();

    void setId(String id);
}
