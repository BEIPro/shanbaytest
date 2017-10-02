package spd.com.shanbaytest.models.Pojo;

/**
 * Created by joe on 17-10-1.
 */

public class WordDetails {

    private Data data;
    private String msg;
    private int status_code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        private String audio;
        private String pronunciation;
        private String definition;
        private String content;

        public String getAudio() {
            return audio;
        }

        public void setAudio(String audio) {
            this.audio = audio;
        }

        public String getPronunciation() {
            return pronunciation;
        }

        public void setPronunciation(String pronunciation) {
            this.pronunciation = pronunciation;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Data [audio=" + audio + ", pronunciation=" + pronunciation + ", definition="
                    + definition + ", content = " + content + "]";
        }
    }

    @Override
    public String toString() {
        return "data = " + data.toString();
    }
}
