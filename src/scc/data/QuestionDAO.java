package scc.data;

public class QuestionDAO {

    private String _rid;
    private String _ts;
    String houseId;

    String userId;

    String questionContent;

    String replyContent;

    public QuestionDAO(){
    }

    public QuestionDAO(Question q){
        this(q.getHouseId(), q.getUserId(), q.getQuestionContent());
    }

    public QuestionDAO(String houseId, String userId, String questionContent){
        this.houseId = houseId;
        this.userId = userId;
        this.questionContent = questionContent;
    }

    public String get_rid() {
        return _rid;
    }
    public void set_rid(String _rid) {
        this._rid = _rid;
    }
    public String get_ts() {
        return _ts;
    }
    public void set_ts(String _ts) {
        this._ts = _ts;
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

    public Question toQuestion(){
        return new Question(houseId, userId, questionContent);
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
