package com.cool.server.service.notify;

import com.cool.pojo.entity.notify.NotifyMessage;

public interface InnerMessageService {

    void saveCommentNotify(NotifyMessage message);

    void saveLikeNotify(NotifyMessage message);

    void saveFollowNotify(NotifyMessage message);

    void savePrivateNotify(NotifyMessage message);

    void saveSystemNotify(NotifyMessage message);
}
