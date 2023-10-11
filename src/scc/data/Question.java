package scc.data;

public class Question {

    String houseId;

    String userId;

    String questionContent;

    String replyContent;

    public Question(String houseId, String userId, String questionContent){
        this.houseId = houseId;
        this.userId = userId;
        this.questionContent = questionContent;
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
        return "Question{" +
                "houseId='" + houseId + '\'' +
                ", userId='" + userId + '\'' +
                ", questionContent='" + questionContent + '\'' +
                ", replyContent='" + replyContent + '\'' +
                '}';
    }
}
