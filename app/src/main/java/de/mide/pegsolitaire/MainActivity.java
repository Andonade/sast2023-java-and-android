package de.mide.pegsolitaire;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static de.mide.pegsolitaire.model.PlaceStatusEnum.SPACE;
import static de.mide.pegsolitaire.model.PlaceStatusEnum.BLOCKED;
import static de.mide.pegsolitaire.model.PlaceStatusEnum.PEG;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import de.mide.pegsolitaire.model.PlaceStatusEnum;
import de.mide.pegsolitaire.model.SpacePosition;

import java.lang.Math;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    public static final String TAG4LOGGING = "PegSolitaire";

    private static final int TEXT_COLOR_BROWN = 0xffa52a2a;
    private static final int TEXT_COLOR_RED = 0xffff0000;

    /**
     * Unicode字符：实心方块
     */
    private static final String TOKEN_MARK = "■";

    /**
     * 用于存储棋盘初始化的数组。
     */
    private static final PlaceStatusEnum[][] PLACE_INIT_ARRAY =
            {
                    {BLOCKED, BLOCKED, PEG, PEG, PEG, BLOCKED, BLOCKED},
                    {BLOCKED, BLOCKED, PEG, PEG, PEG, BLOCKED, BLOCKED},
                    {PEG, PEG, PEG, PEG, PEG, PEG, PEG},
                    {PEG, PEG, PEG, SPACE, PEG, PEG, PEG},
                    {PEG, PEG, PEG, PEG, PEG, PEG, PEG},
                    {BLOCKED, BLOCKED, PEG, PEG, PEG, BLOCKED, BLOCKED},
                    {BLOCKED, BLOCKED, PEG, PEG, PEG, BLOCKED, BLOCKED}
            };

    private final int _sizeColumn = PLACE_INIT_ARRAY.length;

    private final int _sizeRow = PLACE_INIT_ARRAY[0].length;

    /**
     * 用于存储棋盘上的棋子和空位置的数组。
     */
    private PlaceStatusEnum[][] _placeArray = null;

    /**
     * 当前棋盘上的棋子数量。
     */
    private int _numberOfPegs = -1;
    /**
     * 当前执行的步数。
     */
    private int _numberOfSteps = -1;
    /**
     * 选中的棋子是否已经被移动了。
     */
    private boolean _selectedPegMoved = false;

    /**
     * 用于存储棋盘上的棋子的按钮。
     */
    private ViewGroup.LayoutParams _buttonLayoutParams = null;

    /**
     * 用于开始新游戏的按钮。
     */
    private Button _startButton = null;

    /**
     * 棋盘上的棋子和空位置的布局。
     */
    private GridLayout _gridLayout = null;

    private SharedPreferences _data = null;

    /**
     * 用于处理点击棋盘上的棋子的事件。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG4LOGGING, "column=" + _sizeColumn + ", row=" + _sizeRow + "px:");

        _gridLayout = findViewById(R.id.boardGridLayout);

        _data = getSharedPreferences("best_step", 0);

        displayResolutionEvaluate();
        actionBarConfiguration();
        initializeBoard();
    }

    /**
     * 从显示器读取分辨率并将值写入适当的成员变量。
     */
    private void displayResolutionEvaluate() {

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        Log.i(TAG4LOGGING, "Display-Resolution: " + displayWidth + "x" + displayHeight);

        int _sideLengthPlace = displayWidth / _sizeColumn;

        _buttonLayoutParams = new ViewGroup.LayoutParams(_sideLengthPlace,
                _sideLengthPlace);
    }

    /**
     * 初始化操作栏。
     */
    private void actionBarConfiguration() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {

            Toast.makeText(this, "没有操作栏", Toast.LENGTH_LONG).show();
            return;
        }

        actionBar.setTitle("单人跳棋");
    }

    /**
     * 从资源文件加载操作栏菜单项。
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu_items, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 处理操作栏菜单项的选择。
     * 在扩展的版本中，你需要加入更多的菜单项。
     *
     * @param item 选择的菜单项
     * @return true: 选择的菜单项被处理了
     * false: 选择的菜单项没有被处理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_new_game) {
            selectedNewGame();
            return true;
        } else if (item.getItemId() == R.id.action_hof) {
            selectedHOF();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 处理点击"新游戏"按钮的事件。
     * 弹出对话框，询问用户是否要开始新游戏。
     * 如果用户选择"是"，则初始化棋盘，否则不做任何事情。
     */
    public void selectedNewGame() {
        // TODO
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("进行新游戏");
        dialogBuilder.setMessage("是否开始新游戏");
        dialogBuilder.setPositiveButton("是", (DialogInterface, id) -> initializeBoard());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public void selectedHOF() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("名人堂🏆");
        builder.setPositiveButton("确定", (DialogInterface, id) -> {});

        int bestStep = _data.getInt("step", -1);
        String username = _data.getString("username", "");

        if (bestStep == -1) {
            builder.setMessage("还没有历史记录呢");
        } else {
            builder.setMessage("最佳步数: " + bestStep + "\n创造者: " + username);
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * 初始化棋盘上的棋子和空位置。
     */
    private void initializeBoard() {

        if (_gridLayout.getRowCount() == 0) {

            _gridLayout.setColumnCount(_sizeRow);

        } else { // 清除旧的棋盘

            _gridLayout.removeAllViews();
        }

        _numberOfSteps = 0;
        _numberOfPegs = 0;
        _selectedPegMoved = false;
        _placeArray = new PlaceStatusEnum[_sizeColumn][_sizeRow];

        for (int i = 0; i < _sizeColumn; i++) {

            for (int j = 0; j < _sizeRow; j++) {

                PlaceStatusEnum placeStatus = PLACE_INIT_ARRAY[i][j];

                _placeArray[i][j] = placeStatus;

                switch (placeStatus) {

                    case PEG:
                        generateButton(i, j, true);
                        break;

                    case SPACE:
                        generateButton(i, j, false);
                        break;

                    case BLOCKED:
                        Space space = new Space(this); // Dummy-Element
                        _gridLayout.addView(space);
                        break;

                    default:
                        Log.e(TAG4LOGGING, "错误的棋盘状态");

                }
            }
        }

        Log.i(TAG4LOGGING, "棋盘初始化完成");
        updateDisplayStepsNumber();
    }

    /**
     * 生成棋盘上的一个位置。
     * 在基础任务中，棋盘上的棋子直接用字符 TOKEN_MARK 表示。
     * 在扩展任务中，棋盘上的棋子用图片表示。
     */
    private void generateButton(int indexColumn, int indexRow, boolean isPeg) {

        Button button = new Button(this);

        button.setTextSize(22.0f);
        button.setLayoutParams(_buttonLayoutParams);
        button.setOnClickListener(this);
        button.setTextColor(TEXT_COLOR_BROWN);

        SpacePosition pos = new SpacePosition(indexColumn, indexRow);
        button.setTag(pos);

        // TODO
        if (isPeg) {
            button.setText(TOKEN_MARK);
            _numberOfPegs++;
        }
        _gridLayout.addView(button);
    }


    /**
     * 更新操作栏中的步数显示。
     */
    private void updateDisplayStepsNumber() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("执行步数：" + _numberOfSteps);
        }
    }

    /**
     * 处理棋盘上的点击事件。
     * 如果被点击的按钮是一个棋子，那么它将被改变选中状态。
     * 也就是说，如果它之前没有被选中，这个棋子会变为红色，
     * 同时，此前被选中的棋子（如果有）将变为棕色。
     * 或者如果它已经被选中，那么它自己将变为棕色。
     * 如果被点击的按钮是一个空位置，那么试图将被选中的棋子移动到该位置。
     * 如果移动成功，你需要更新棋盘上的棋子和空位置。
     * 如果移动失败，你需要显示一个错误信息。
     *
     * @param view 被点击的按钮
     *

     */
    @Override
    public void onClick(View view) {

        Button clickedButton = (Button) view;

        SpacePosition targetPosition = (SpacePosition) clickedButton.getTag();
        // 获取被点击的按钮的位置
        int indexColumn = targetPosition.getIndexColumn();
        int indexRow = targetPosition.getIndexRow();
        PlaceStatusEnum placeStatus = _placeArray[indexColumn][indexRow];

        switch (placeStatus) {

            case PEG:
                // TODO
                _selectedPegMoved = false;
                if (clickedButton.getCurrentTextColor() == TEXT_COLOR_RED) {
                    clickedButton.setTextColor(TEXT_COLOR_BROWN);
                } else {
                    for (int i = 0; i < _sizeColumn; i++) {
                        for (int j = 0; j < _sizeRow; j++) {
                            if (_placeArray[i][j] == PEG) {
                                Button button = getButtonFromPosition(new SpacePosition(i, j));
                                if (button.getCurrentTextColor() == TEXT_COLOR_RED) {
                                    button.setTextColor(TEXT_COLOR_BROWN);
                                    break;
                                }
                            }
                        }
                    }
                    clickedButton.setTextColor(TEXT_COLOR_RED);
                }
                break;

            case SPACE:
                // TODO
                for (int i = 0; i < _sizeColumn; i++) {
                    for (int j = 0; j < _sizeRow; j++) {
                        if (_placeArray[i][j] == PEG) {
                            Button button = getButtonFromPosition(new SpacePosition(i, j));
                            if (button.getCurrentTextColor() == TEXT_COLOR_RED) {
                                SpacePosition startPosition = (SpacePosition) button.getTag();
                                SpacePosition skippedPosition = getSkippedPosition(startPosition, targetPosition);
                                if (skippedPosition == null) {
                                    Toast.makeText(this, "无法跳到该位置", Toast.LENGTH_SHORT).show();
                                } else {
                                    jumpToPosition(button, clickedButton, getButtonFromPosition(skippedPosition));
                                }
                                return;
                            }
                        }
                    }
                }
                break;

            default:
                Log.e(TAG4LOGGING, "错误的棋盘状态" + placeStatus);
        }
    }

    /**
     * 执行跳跃。仅当确定移动合法时才可以调用该方法。
     * 数组中三个位置的状态，和总棋子数发生变化。
     * 同时，在移动后，你需要检查是否已经结束游戏。
     *
     * @param startButton 被选中的棋子
     * @param targetButton 被选中的空位置
     * @param skippedButton 被跳过的棋子
     *
     */
    private void jumpToPosition(Button startButton, Button targetButton, Button skippedButton) {

        // TODO
        startButton.setText("");
        skippedButton.setText("");
        targetButton.setText(TOKEN_MARK);

        startButton.setTextColor(TEXT_COLOR_BROWN);
        targetButton.setTextColor(TEXT_COLOR_RED);

        SpacePosition startPos = (SpacePosition) startButton.getTag();
        SpacePosition targetPos = (SpacePosition) targetButton.getTag();
        SpacePosition skippedPos = (SpacePosition) skippedButton.getTag();

        _placeArray[startPos.getIndexColumn()][startPos.getIndexRow()] = SPACE;
        _placeArray[targetPos.getIndexColumn()][targetPos.getIndexRow()] = PEG;
        _placeArray[skippedPos.getIndexColumn()][skippedPos.getIndexRow()] = SPACE;

        if (!_selectedPegMoved) {
            _numberOfSteps++;
        }
        _selectedPegMoved = true;

        _numberOfPegs--;
        updateDisplayStepsNumber();
        if (_numberOfPegs == 1) {
            showVictoryDialog();
        } else if (!has_movable_places()) {
            showFailureDialog();
        }
    }

    /**
     * 返回位置对应的按钮。
     *
     * @param position 位置
     * @return 按钮
     */
    private Button getButtonFromPosition(SpacePosition position) {

        int index = position.getPlaceIndex(_sizeRow);

        return (Button) _gridLayout.getChildAt(index);
    }

    /**
     * 显示一个对话框，表明游戏已经胜利（只剩下一个棋子）。
     * 点击对话框上的按钮，可以重新开始游戏。
     * 在扩展版本中，你需要在这里添加一个输入框，让用户输入他的名字。
     */
    private void showVictoryDialog() {
        EditText usernameInput = new EditText(this);
        usernameInput.setHint("请输入你的名字");

        LinearLayout layout = new LinearLayout(this);
        layout.addView(usernameInput);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) usernameInput.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.weight = 1;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("胜利");
        dialogBuilder.setMessage("你赢了！");
        dialogBuilder.setView(layout);
        dialogBuilder.setPositiveButton("再来一局", (dialogInterface, i) -> {
            final String username = usernameInput.getText().toString();
            int bestStep = _data.getInt("step", -1);
            if (bestStep > _numberOfSteps || bestStep == -1) {
                SharedPreferences.Editor editor = _data.edit();
                editor.putInt("step", _numberOfSteps);
                editor.putString("username", username);
                editor.apply();
            }
            initializeBoard();  // 重新开始游戏
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * 显示一个对话框，表明游戏已经失败（没有可移动的棋子）。
     * 点击对话框上的按钮，可以重新开始游戏。
     */
    private void showFailureDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("失败");
        dialogBuilder.setMessage("你输了！");
        dialogBuilder.setPositiveButton("再来一局", (dialogInterface, i) -> {
            initializeBoard();  // 重新开始游戏
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * 给定一个起始位置和目标位置。
     * 如果移动合法，返回被跳过的位置。
     * 如果移动不合法，返回 {@code null}。
     * 移动合法的定义，参见作业文档。
     *
     * @param startPos  起始位置
     * @param targetPos 目标位置
     * @return 移动合法时，返回一个新{@code SpacePosition}
     * 表示被跳过的位置；否则返回 {@code null}
     */
    private SpacePosition getSkippedPosition(SpacePosition startPos, SpacePosition targetPos) {
        // TODO
        int startColumn = startPos.getIndexColumn();
        int startRow = startPos.getIndexRow();
        int targetColumn = targetPos.getIndexColumn();
        int targetRow = targetPos.getIndexRow();
        if (startRow != targetRow && startColumn != targetColumn) {
            return null;
        } else if (startColumn == targetColumn) {
            if (Math.abs(startRow - targetRow) == 1) {
                return null;
            }
            int direction = (startRow < targetRow) ? 1 : -1;
            if (_placeArray[startColumn][targetRow - direction] != PEG) {
                return null;
            } else {
                int row = startRow + direction;
                while (row != targetRow - direction) {
                    if (_placeArray[startColumn][row] == PEG) {
                        return null;
                    }
                    row += direction;
                }
                return new SpacePosition(startColumn, targetRow - direction);
            }
        } else {
            if (Math.abs(startColumn - targetColumn) == 1) {
                return null;
            }
            int direction = (startColumn < targetColumn) ? 1 : -1;
            if (_placeArray[targetColumn - direction][startRow] != PEG) {
                return null;
            } else {
                int column = startColumn + direction;
                while (column != targetColumn - direction) {
                    if (_placeArray[column][startRow] == PEG) {
                        return null;
                    }
                    column += direction;
                }
                return new SpacePosition(targetColumn - direction, startRow);
            }
        }
    }

    /**
     * 返回是否还有可移动的位置。
     *
     * @return 如果还有可移动的位置，返回 {@code true}
     * 否则返回 {@code false}
     */
    private Boolean has_movable_places(){
        for(int i = 0; i < _sizeColumn; i++){
            for(int j = 0; j < _sizeRow; j++){
                if(_placeArray[i][j] == PEG){
                    // TODO
                    for (int k = j - 1; k > 0; k--) {
                        if (_placeArray[i][k] == SPACE) {
                            if (getSkippedPosition(new SpacePosition(i, j), new SpacePosition(i, k)) != null) {
                                return true;
                            }
                        }
                    }
                    for (int k = j + 1; k < _sizeRow; k++) {
                        if (_placeArray[i][k] == SPACE) {
                            if (getSkippedPosition(new SpacePosition(i, j), new SpacePosition(i, k)) != null) {
                                return true;
                            }
                        }
                    }
                    for (int l = i - 1; l > 0; l--) {
                        if (_placeArray[l][j] == SPACE) {
                            if (getSkippedPosition(new SpacePosition(i, j), new SpacePosition(l, j)) != null) {
                                return true;
                            }
                        }
                    }
                    for (int l = i + 1; l < _sizeColumn; l++) {
                        if (_placeArray[l][j] == SPACE) {
                            if (getSkippedPosition(new SpacePosition(i, j), new SpacePosition(l, j)) != null) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
