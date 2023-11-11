package scc.data;

public class Question {

    private String id;
    private String houseId;
    private String userId;
    private String questionContent;
    private String replyContent;

    public Question(){}

    public Question(String houseId, String userId, String questionContent){
        this.houseId = houseId;
        this.userId = userId;
        this.questionContent = questionContent;
    }

    public String getId() {
        return id;
    }
    public String getHouseId() {
        return houseId;
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
        return "Question [id=" + id + ", houseId='" + houseId  + ", userId='" + userId +
                ", questionContent='" + questionContent + ", replyContent='" + replyContent +
                ']';
    }
}
