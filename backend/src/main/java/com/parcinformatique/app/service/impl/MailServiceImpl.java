package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.entity.EmailVerificationToken;
import com.parcinformatique.app.entity.PasswordResetToken;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@parc.local}")
    private String mailFrom;

    @Value("${app.frontend-base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Value("${app.mail.fail-on-error:false}")
    private boolean failOnError;

    @Override
    public void sendVerificationEmail(User user, EmailVerificationToken token) {
        String actionUrl = frontendBaseUrl + "/verify-email?token=" + token.getToken();
        sendMail(
            user.getEmail(),
            "Verification de votre compte ParcFlow",
            """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a">
                  <h2>Bienvenue sur ParcFlow</h2>
                  <p>Bonjour %s,</p>
                  <p>Veuillez verifier votre adresse email pour activer votre compte.</p>
                  <p><a href="%s" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#0ea5e9;color:#fff;text-decoration:none">Verifier mon email</a></p>
                  <p>Si le bouton ne fonctionne pas, copiez ce lien :</p>
                  <p>%s</p>
                </div>
                """.formatted(user.getFirstName(), actionUrl, actionUrl)
        );
    }

    @Override
    public void sendPasswordResetEmail(User user, PasswordResetToken token) {
        String actionUrl = frontendBaseUrl + "/reset-password?token=" + token.getToken();
        sendMail(
            user.getEmail(),
            "Reinitialisation de votre mot de passe ParcFlow",
            """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a">
                  <h2>Reinitialisation du mot de passe</h2>
                  <p>Bonjour %s,</p>
                  <p>Nous avons recu une demande de reinitialisation de votre mot de passe.</p>
                  <p><a href="%s" style="display:inline-block;padding:12px 18px;border-radius:10px;background:#16a34a;color:#fff;text-decoration:none">Definir un nouveau mot de passe</a></p>
                  <p>Si vous n'etes pas a l'origine de cette demande, ignorez simplement cet email.</p>
                  <p>Lien direct : %s</p>
                </div>
                """.formatted(user.getFirstName(), actionUrl, actionUrl)
        );
    }

    private void sendMail(String to, String subject, String htmlBody) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MailException | jakarta.mail.MessagingException ex) {
            if (failOnError) {
                throw new IllegalStateException("Impossible d'envoyer l'email", ex);
            }
            log.warn("Envoi d'email impossible vers {}: {}", to, ex.getMessage());
        }
    }
}
