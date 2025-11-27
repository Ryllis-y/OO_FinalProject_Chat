package com.example.chat.service;

import com.example.chat.common.model.Message;

public interface MessageService {
    // 修改这个方法的签名，多加一个 atUsers 参数
    Message processAndSaveMsg(String fromUser, String toUser, String content, boolean isGroup, java.util.List<String> atUsers);
    boolean recallMessage(String msgId, String operator);

    /**
     * 解析给定文本中的URL并返回链接预览对象
     * @param text 包含URL的文本
     * @return LinkPreview 预览对象，如果解析失败或没有链接则返回 null
     */
    com.example.chat.common.model.LinkPreview parseUrlPreview(String text);
}