package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import java.util.List;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class Comment {
    public User user;
    public String body;
    public List<String> imageUrls;
    public Integer votesUp;
    public Integer votesDown;
    public List<Comment> comments;
}
