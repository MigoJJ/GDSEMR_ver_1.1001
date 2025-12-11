package com.emr.gds.main.template;

public class TemplateModel {
    private final int id;
    private final String name;
    private final String content;

    public TemplateModel(int id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return name;
    }
}
