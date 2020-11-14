package polytech.sacc.onfine.entity;


import java.util.List;

public interface MessageRepository {

    /** Save message to persistent storage. */
    void save(Message message);

    /**
     * Retrieve most recent stored messages.
     *
     * @param limit number of messages
     * @return list of messages
     */
    List<Message> retrieve(int limit);

    /** Save claim to persistent storage. */
    void saveClaim(String claim);

    /**
     * Retrieve most recent stored claims.
     *
     * @param limit number of messages
     * @return list of claims
     */
    List<String> retrieveClaims(int limit);

    /** Save token to persistent storage. */
    void saveToken(String token);

    /**
     * Retrieve most recent stored tokens.
     *
     * @param limit number of messages
     * @return list of tokens
     */
    List<String> retrieveTokens(int limit);
}
