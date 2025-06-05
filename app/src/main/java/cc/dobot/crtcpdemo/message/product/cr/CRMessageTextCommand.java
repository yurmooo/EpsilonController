package cc.dobot.crtcpdemo.message.product.cr;

import com.xuhao.didi.core.pojo.OriginalData;

import cc.dobot.crtcpdemo.message.base.BaseMessage;

public class CRMessageTextCommand extends BaseMessage {
    private String messageString;

    public void setMessageString(String msg) {
        this.messageString = msg;
        setMessageContent(msg.getBytes());
    }

    @Override
    public String getMessageStringContent() {
        return messageString;
    }

    @Override
    public void constructSendData() {
        // Не нужен, потому что мы напрямую задаём messageContent
    }

    @Override
    public void transformReceiveData2Object(OriginalData data) {

    }
}