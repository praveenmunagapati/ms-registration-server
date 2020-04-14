package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 3, 2007
 * Time: 9:48:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class Reader {
    private Integer reader_seq_id;
    private String reader_type;
    private String file_ext;
    private String reader_desc;


    public Reader() {
    }


    public Integer getReader_seq_id() {
        return reader_seq_id;
    }

    public void setReader_seq_id(Integer reader_seq_id) {
        this.reader_seq_id = reader_seq_id;
    }

    public String getReader_type() {
        return reader_type;
    }

    public void setReader_type(String reader_type) {
        this.reader_type = reader_type;
    }

    public String getFile_ext() {
        return file_ext;
    }

    public void setFile_ext(String file_ext) {
        this.file_ext = file_ext;
    }

    public String getReader_desc() {
        return reader_desc;
    }

    public void setReader_desc(String reader_desc) {
        this.reader_desc = reader_desc;
    }
}
