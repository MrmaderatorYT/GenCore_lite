package com.ccs.gencorelite.compiler;

import android.content.Context;
import android.content.Intent;

public class TermuxExecutor {

    public static void executeTermuxCommand(Context context, String command) {
        Intent intent = new Intent("com.termux.service_execute");
        intent.setClassName("com.termux", "com.termux.app.RunCommandService");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash");
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[] { "-c", command });
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
        context.startService(intent);
    }
}
