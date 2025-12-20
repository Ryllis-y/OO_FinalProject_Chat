package com.example.chat.util;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 敏感词过滤器 - 基于AC自动机算法
 * AC自动机（Aho-Corasick）是一种多模式字符串匹配算法
 * 可以在O(n+m)时间复杂度内同时匹配多个模式串
 */
@Component
public class SensitiveWordFilter {
    
    // AC自动机的根节点
    private TrieNode root;
    
    // 敏感词列表（不超过20个）
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
        "傻逼", "sb", "弱智", "白痴", "脑残", 
        "废物", "垃圾", "蠢货", "混蛋", "狗屎",
        "王八蛋", "猪头", "笨蛋", "蠢材", "饭桶",
        "窝囊废", "死鬼", "臭不要脸", "变态", "神经病"
    );
    
    /**
     * Trie树节点
     */
    private static class TrieNode {
        // 子节点映射
        Map<Character, TrieNode> children = new HashMap<>();
        // 失败指针（用于AC自动机的跳转）
        TrieNode fail = null;
        // 是否为敏感词结尾
        boolean isEnd = false;
        // 敏感词长度（用于替换）
        int length = 0;
    }
    
    /**
     * 初始化AC自动机
     */
    @PostConstruct
    public void init() {
        root = new TrieNode();
        buildTrie();
        buildFailPointer();
        System.out.println("敏感词过滤器初始化完成，共加载 " + SENSITIVE_WORDS.size() + " 个敏感词");
    }
    
    /**
     * 构建Trie树
     */
    private void buildTrie() {
        for (String word : SENSITIVE_WORDS) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);
            }
            node.isEnd = true;
            node.length = word.length();
        }
    }
    
    /**
     * 构建失败指针（AC自动机的核心）
     */
    private void buildFailPointer() {
        Queue<TrieNode> queue = new LinkedList<>();
        
        // 根节点的所有子节点的失败指针都指向根节点
        for (TrieNode child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }
        
        // BFS构建失败指针
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            
            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();
                
                // 从当前节点的失败指针开始查找
                TrieNode failNode = current.fail;
                while (failNode != null && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }
                
                // 设置失败指针
                if (failNode != null) {
                    child.fail = failNode.children.get(c);
                } else {
                    child.fail = root;
                }
                
                queue.offer(child);
            }
        }
    }
    
    /**
     * 过滤敏感词，将敏感词替换为*
     * @param text 原始文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        char[] chars = text.toCharArray();
        boolean[] mask = new boolean[chars.length]; // 标记哪些位置需要替换
        
        TrieNode node = root;
        
        // AC自动机匹配
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            // 如果当前节点没有这个字符，沿着失败指针跳转
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            
            // 移动到下一个节点
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }
            
            // 检查是否匹配到敏感词
            TrieNode temp = node;
            while (temp != root) {
                if (temp.isEnd) {
                    // 标记需要替换的位置
                    int start = i - temp.length + 1;
                    for (int j = start; j <= i; j++) {
                        mask[j] = true;
                    }
                }
                temp = temp.fail;
            }
        }
        
        // 替换敏感词为*
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (mask[i]) {
                result.append('*');
            } else {
                result.append(chars[i]);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 检查文本中是否包含敏感词
     * @param text 待检查的文本
     * @return 如果包含敏感词返回true，否则返回false
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        TrieNode node = root;
        
        for (char c : text.toCharArray()) {
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }
            
            TrieNode temp = node;
            while (temp != root) {
                if (temp.isEnd) {
                    return true;
                }
                temp = temp.fail;
            }
        }
        
        return false;
    }
}

