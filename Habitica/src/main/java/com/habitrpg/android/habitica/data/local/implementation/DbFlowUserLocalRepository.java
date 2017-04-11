package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import rx.Observable;

public class DbFlowUserLocalRepository implements UserLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<HabitRPGUser> getUser(String userID) {
        return Observable.defer(() -> Observable.just(new Select()
        .from(HabitRPGUser.class).where(Condition.column("id").eq(userID)).querySingle()));
    }
}