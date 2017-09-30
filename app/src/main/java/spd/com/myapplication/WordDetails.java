package spd.com.myapplication;

/**
 * Created by linus on 17-10-1.
 */

public class WordDetails {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    class Data {
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
