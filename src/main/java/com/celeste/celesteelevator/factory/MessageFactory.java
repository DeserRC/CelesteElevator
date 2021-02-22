package com.celeste.celesteelevator.factory;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import com.celeste.celesteelevator.util.impl.BarUtil;
import com.celeste.celesteelevator.util.impl.ChatUtil;
import com.celeste.celesteelevator.util.impl.TitleUtil;
import lombok.Getter;

@Getter
public class MessageFactory {

    private final ChatUtil chatUtil;
    private final BarUtil barUtil;
    private final TitleUtil titleUtil;
    
    private final MessageAdapter messageAdapter;

    public MessageFactory(final CelesteElevator plugin) {
        this.chatUtil = new ChatUtil(plugin);
        this.barUtil = new BarUtil(plugin);
        this.titleUtil = new TitleUtil(plugin);

        this.messageAdapter = new MessageAdapter(plugin, this);
    }

}
