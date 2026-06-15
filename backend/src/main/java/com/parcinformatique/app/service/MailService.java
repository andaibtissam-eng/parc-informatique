package com.parcinformatique.app.service;

import com.parcinformatique.app.entity.EmailVerificationToken;
import com.parcinformatique.app.entity.PasswordResetToken;
import com.parcinformatique.app.entity.User;

public interface MailService {

    void sendVerificationEmail(User user, EmailVerificationToken token);

    void sendPasswordResetEmail(User user, PasswordResetToken token);
}
