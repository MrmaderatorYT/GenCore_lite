package com.ccs.romanticadventure.system;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ccs.romanticadventure.MainActivity;

public class ExitConfirmationDialog {

    public static void showExitConfirmationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Ви впевнені, що хочете вийти?");
        builder.setPositiveButton("Так", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Тут можна додати просто finish(), а не створювати функцію exit
                if (context instanceof MainActivity) {
                    ((MainActivity) context).exit();
                }
            }
        });
        builder.setNegativeButton("Відмна", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Закриваємо діалогове вікно
                dialog.dismiss();
            }
        });
        //показуємо діалогове вікно, яке запитує вихід
        builder.show();
    }
}
