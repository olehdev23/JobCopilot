package com.example.telegrambot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "usr")
public class User {

    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    @Column(columnDefinition = "TEXT")
    private String cv;
    @Column(columnDefinition = "TEXT")
    private String preferences;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private ConversationState conversationState;
}