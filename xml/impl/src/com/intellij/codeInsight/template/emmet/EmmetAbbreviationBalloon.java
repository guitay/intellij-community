package com.intellij.codeInsight.template.emmet;

import com.intellij.codeInsight.template.CustomTemplateCallback;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.xml.XmlBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class EmmetAbbreviationBalloon {
  private final String myAbbreviationsHistoryKey;
  private final String myLastAbbreviationKey;
  private final Callback myCallback;

  public EmmetAbbreviationBalloon(@NotNull String abbreviationsHistoryKey,
                                  @NotNull String lastAbbreviationKey,
                                  @NotNull Callback callback) {
    myAbbreviationsHistoryKey = abbreviationsHistoryKey;
    myLastAbbreviationKey = lastAbbreviationKey;
    myCallback = callback;
  }

  public void show(@NotNull final CustomTemplateCallback customTemplateCallback) {
    final TextFieldWithStoredHistory field = new TextFieldWithStoredHistory(myAbbreviationsHistoryKey);
    final Dimension fieldPreferredSize = field.getPreferredSize();
    field.setPreferredSize(new Dimension(Math.max(220, fieldPreferredSize.width), fieldPreferredSize.height));
    field.setHistorySize(10);
    final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
    final BalloonImpl balloon = (BalloonImpl)popupFactory.createDialogBalloonBuilder(field, XmlBundle.message("emmet.title"))
      .setCloseButtonEnabled(false)
      .setBlockClicksThroughBalloon(true)
      .setAnimationCycle(0)
      .setHideOnKeyOutside(true)
      .setHideOnClickOutside(true)
      .createBalloon();

    field.addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        validateTemplateKey(field, balloon, field.getText(), customTemplateCallback);
      }
    });
    field.addKeyboardListener(new KeyAdapter() {
      @Override
      public void keyPressed(@NotNull KeyEvent e) {
        if (!field.isPopupVisible()) {
          switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
              final String abbreviation = field.getText();
              if (validateTemplateKey(field, balloon, abbreviation, customTemplateCallback)) {
                myCallback.onEnter(abbreviation);
                PropertiesComponent.getInstance().setValue(myLastAbbreviationKey, abbreviation);
                field.addCurrentTextToHistory();
                balloon.hide();
              }
              break;
            case KeyEvent.VK_ESCAPE:
              balloon.hide(false);
              break;
          }
        }
      }
    });

    balloon.addListener(new JBPopupListener.Adapter() {
      @Override
      public void beforeShown(LightweightWindowEvent event) {
        field.setText(PropertiesComponent.getInstance().getValue(myLastAbbreviationKey, ""));
      }
    });
    balloon.show(popupFactory.guessBestPopupLocation(customTemplateCallback.getEditor()), Balloon.Position.below);

    final IdeFocusManager focusManager = IdeFocusManager.getInstance(customTemplateCallback.getProject());
    focusManager.doWhenFocusSettlesDown(new Runnable() {
      @Override
      public void run() {
        focusManager.requestFocus(field, true);
        field.selectText();
      }
    });
  }

  private static boolean validateTemplateKey(@NotNull TextFieldWithHistory field,
                                             @Nullable Balloon balloon,
                                             @NotNull String abbreviation,
                                             @NotNull CustomTemplateCallback callback) {
    final boolean correct = ZenCodingTemplate.checkTemplateKey(abbreviation, callback);
    field.getTextEditor().setBackground(correct ? LightColors.SLIGHTLY_GREEN : LightColors.RED);
    if (balloon != null && !balloon.isDisposed()) {
      balloon.revalidate();
    }
    return correct;
  }


  public interface Callback {
    void onEnter(@NotNull String abbreviation);
  }
}
