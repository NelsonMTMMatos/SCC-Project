package scc.data;

import java.util.UUID;

public class QuestionDAO {

    private String _rid;
    private String _ts;
    private String id;
    private String houseId;
    private String userId;
    private String questionContent;
    private String replyContent;

    public QuestionDAO(){}

    public QuestionDAO(Question q){
        this(q.getUserId(), q.getQuestionContent());
    }

    public QuestionDAO(String userId, String questionContent){
        super();
        this.id = UUID.randomUUID().toString();
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

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Question toQuestion(){
        return new Question(userId, questionContent);
    }

    @Override
    public String toString() {
        return "QuestionDAO{" +
                "id='" + id + '\'' +
                ", houseId='" + houseId + '\'' +
                ", userId='" + userId + '\'' +
                ", questionContent='" + questionContent + '\'' +
                ", replyContent='" + replyContent + '\'' +
                '}';
    }
}
