package com.kuklin.telegrammvc.context;

/**
 * Telegram user who sent the update.
 *
 * @param id           Telegram user id
 * @param username     username without {@code @}, may be {@code null}
 * @param firstName    first name
 * @param lastName     last name, may be {@code null}
 * @param languageCode IETF language tag of the user's client, may be {@code null}
 */
public record ChatUser(long id, String username, String firstName, String lastName, String languageCode) {
}
