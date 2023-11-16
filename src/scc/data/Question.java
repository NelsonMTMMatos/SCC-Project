package scc.data;

public class Question {
    private String userId;
    private String questionContent;
    private String replyContent;

    public Question(){}

    public Question(String userId, String questionContent){
        this.userId = userId;
        this.questionContent = questionContent;
    }

    public String getUserId() {
        return userId;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    @Override
    public String toString() {
        return "Question{" +
                "userId='" + userId + '\'' +
                ", questionContent='" + questionContent + '\'' +
                ", replyContent='" + replyContent + '\'' +
                '}';
    }
}
