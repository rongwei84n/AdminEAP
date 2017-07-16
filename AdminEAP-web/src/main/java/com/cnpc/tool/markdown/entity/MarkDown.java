package com.cnpc.tool.markdown.entity;

import com.cnpc.framework.annotation.Header;
import com.cnpc.framework.base.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "tbl_markdown")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "fieldHandler" })
public class MarkDown extends BaseEntity {

    private static final long serialVersionUID = 7823893456789085L;


    @Header(name="标题")
    @Column(name="title")
    private String title;

    @Header(name="关键字")
    @Column(name="keywords")
    private String keywords;

    @Header(name="内容")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type="text")
    @Column(name="content")
    private String content;

    @Header(name="用户")
    @Column(name="user_id")
    private String userId;

    @Header(name="编码")
    @Column(name="code")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
