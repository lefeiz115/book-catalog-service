package com.eyebuy.bookcatalog.service.template;

import com.eyebuy.bookcatalog.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("json")
public class JsonBookExporter extends BookExportTemplate {

    private final ObjectMapper objectMapper;

    public JsonBookExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected String buildHeader() {
        return "[";
    }

    @Override
    protected String buildBody(List<Book> books) {
        try {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (Book book : books) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("id", book.getId());
                node.put("title", book.getTitle());
                node.put("author", book.getAuthor());
                node.put("isbn", book.getIsbn() != null ? book.getIsbn() : "");
                node.put("genre", book.getGenre());
                node.put("price", book.getPrice() != null ? book.getPrice().toPlainString() : "");
                node.put("stock", book.getStock() != null ? book.getStock() : 0);
                node.put("pages", book.getPages() != null ? book.getPages() : 0);
                node.put("publisher", book.getPublisher() != null ? book.getPublisher() : "");
                node.put("publishDate", book.getPublishDate() != null ? book.getPublishDate().toString() : "");
                node.put("description", book.getDescription() != null ? book.getDescription() : "");
                node.put("active", book.getActive() != null ? book.getActive() : false);
                node.put("createTime", book.getCreateTime() != null ? book.getCreateTime().toString() : "");
                node.put("updateTime", book.getUpdateTime() != null ? book.getUpdateTime().toString() : "");
                arrayNode.add(node);
            }
            String json = objectMapper.writeValueAsString(arrayNode);
            return json.substring(1, json.length() - 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize books to JSON", e);
        }
    }

    @Override
    protected String buildFooter() {
        return "]";
    }

    @Override
    protected String getContentType() {
        return "application/json; charset=UTF-8";
    }

    @Override
    protected String generateFileName() {
        return "books_export_" + System.currentTimeMillis() + ".json";
    }
}
