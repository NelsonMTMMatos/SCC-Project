package scc.data;

public class QuestionDAO {

    private String _rid;
    private String _ts;
    private String id;
    private String houseId;
    private String userId;
    private String questionContent;
    private String replyContent;

    public QuestionDAO(){
    }

    public QuestionDAO(Question q){
        this(q.getId(), q.getHouseId(), q.getUserId(), q.getQuestionContent());
    }

    public QuestionDAO(String id, String houseId, String userId, String questionContent){
        super();
        this.id = id;
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

    public String getId() { return id; }
    public void setId(String id) {
        this.id = id;
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
        return "Question[id=" + id + ", houseId=" + houseId + ", userId=" + userId +
                ", questionContent=" + questionContent + ", replyContent=" + replyContent +
                "]";
    }
}
