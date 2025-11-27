package com.example.chat.service.impl;

import com.example.chat.common.model.LinkPreview;
import com.example.chat.common.model.Message;
import com.example.chat.service.MessageService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public Message processAndSaveMsg(String fromUser, String toUser, String content, boolean isGroup, List<String> atUsers) {
        // ... (existing implementation)
    }

    @Override
    public boolean recallMessage(String msgId, String operator) {
        return true; // 假装撤回成功
    }

    /**
     * 解析 URL 预览 (耗时操作)
     */
    @Override
    public LinkPreview parseUrlPreview(String text) {
        // 简单提取 http 链接
        if (text == null || !text.contains("http")) {
            return null;
        }

        try {
            // 提取第一个链接
            int start = text.indexOf("http");
            int end = text.indexOf(" ", start);
            if (end == -1) {
                end = text.length();
            }
            String url = text.substring(start, end);

            // Jsoup 抓取
            Document doc = Jsoup.connect(url)
                    .timeout(3000) // 3秒超时
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // 设置一个常见的 User-Agent
                    .get();

            String title = doc.title();
            // 尝试获取 og:image
            org.jsoup.nodes.Element imgEl = doc.select("meta[property=og:image]").first();
            String img = (imgEl != null) ? imgEl.attr("content") : null;

            // 尝试获取 description
            org.jsoup.nodes.Element descEl = doc.select("meta[name=description], meta[property=og:description]").first();
            String desc = (descEl != null) ? descEl.attr("content") : url;

            // 避免标题或描述过长
            if (title != null && title.length() > 100) {
                title = title.substring(0, 100) + "...";
            }
            if (desc != null && desc.length() > 200) {
                desc = desc.substring(0, 200) + "...";
            }

            return new LinkPreview(title, desc, img);
        } catch (Exception e) {
            // 解析失败忽略
            // e.printStackTrace(); // 生产环境应使用日志
            return null;
        }
    }
}