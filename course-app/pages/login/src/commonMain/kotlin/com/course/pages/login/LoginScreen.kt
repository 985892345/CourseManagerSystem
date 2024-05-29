package com.course.pages.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.base.account.Account
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.debug.logg
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.serializable.StringStateSerializable
import com.course.components.utils.source.ResponseException
import com.course.components.utils.source.Source
import com.course.components.utils.source.onFailure
import com.course.components.utils.source.onSuccess
import com.course.pages.main.MainScreen
import com.course.pages.main.sHasLogin
import com.course.source.app.account.AccountApi
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 20:06
 */
@Serializable
@ObjectSerializable
@ImplProvider(clazz = BaseScreen::class, name = "login")
class LoginScreen : BaseScreen() {

  @Serializable(StringStateSerializable::class)
  private val username = mutableStateOf("")

  @Serializable(StringStateSerializable::class)
  private val password = mutableStateOf("")

  @Composable
  override fun ScreenContent() {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier.align(Alignment.Center)
          .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
      ) {
        Text(
          modifier = Modifier,
          text = "登录",
          fontSize = 34.sp,
          color = LocalAppColors.current.tvLv2
        )
        Text(
          modifier = Modifier.padding(top = 16.dp),
          text = "👏欢迎使用课表管理系统",
          fontSize = 18.sp,
          color = LocalAppColors.current.tvLv2.copy(alpha = 0.6F),
        )
        UsernameCompose()
        PasswordCompose()
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
            modifier = Modifier.align(Alignment.CenterEnd)
              .clickableCardIndicator(4.dp) {
                toast("未实现")
              }.padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
            text = "忘记密码？",
            fontSize = 12.sp,
            color = Color(0xFFABBCD8)
          )
        }
        LoginBtnCompose()
      }
    }
  }

  @Composable
  private fun ColumnScope.UsernameCompose() {
    Row(
      modifier = Modifier.padding(top = 28.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
        imageVector = Icons.Default.AccountCircle,
        contentDescription = null,
      )
      NoBorderOutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = username.value,
        singleLine = true,
        onValueChange = {
          username.value = it
        },
        label = {
          Text(text = "请输入学号或教工号")
        },
      )
    }
  }

  @Composable
  private fun ColumnScope.PasswordCompose() {
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val oldText = remember { mutableStateOf("") }
    val visualTransformationAll = remember {
      PasswordVisualTransformation()
    }
    val visualTransformationLast = remember {
      VisualTransformation {
        TransformedText(
          AnnotatedString(
            '\u2022'.toString().repeat(oldText.value.length) +
                password.value.substringAfter(oldText.value)
          ),
          OffsetMapping.Identity
        )
      }
    }
    val visualTransformation = remember { mutableStateOf(visualTransformationLast) }
    Row(
      modifier = Modifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        imageVector = Icons.Default.Key,
        contentDescription = null,
      )
      NoBorderOutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = password.value,
        visualTransformation = visualTransformation.value,
        onValueChange = {
          if (it.length > password.value.length) {
            visualTransformation.value = visualTransformationLast
            oldText.value = password.value
          } else {
            visualTransformation.value = visualTransformationAll
            oldText.value = it
          }
          password.value = it
        },
        singleLine = true,
        label = {
          Text(text = "请输入密码")
        },
        keyboardActions = KeyboardActions {
          login(coroutineScope, navigator)
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
      )
    }
    LaunchedEffect(Unit) {
      snapshotFlow { password.value }.collectLatest {
        delay(1.seconds)
        visualTransformation.value = visualTransformationAll
      }
    }
  }

  private val isClicked = mutableStateOf(false)

  @Composable
  private fun ColumnScope.LoginBtnCompose() {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    Box(
      modifier = Modifier.align(Alignment.CenterHorizontally)
        .padding(top = 100.dp)
        .height(52.dp),
      contentAlignment = Alignment.Center,
    ) {
      AnimatedContent(
        targetState = isClicked.value,
      ) {
        if (!it) {
          Card(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            backgroundColor = Color(0xFF4A44E4),
            shape = CircleShape,
          ) {
            Box(
              modifier = Modifier.clickable {
                login(coroutineScope, navigator)
              },
              contentAlignment = Alignment.Center
            ) {
              Text(text = "登 录", color = Color.White, fontSize = 18.sp)
            }
          }
        } else {
          CircularProgressIndicator()
        }
      }
    }
  }

  private fun login(
    coroutineScope: CoroutineScope,
    navigator: Navigator?,
  ) {
    if (isClicked.value) return
    if (username.value.isEmpty()) {
      toast("未输入账号")
      return
    }
    if (password.value.isEmpty()) {
      toast("未输入密码")
      return
    }
    isClicked.value = true
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(AccountApi::class)
          .login(username.value, password.value)
      }.tryThrowCancellationException().onSuccess { wrapper ->
        wrapper.onSuccess {
          if (!Account.refreshAccount()) {
            toast("用户信息刷新失败")
          } else {
            sHasLogin = true
          }
          navigator?.replace(MainScreen())
        }.onFailure {
          isClicked.value = false
          toast(it.info)
        }
      }.onFailure {
        isClicked.value = false
        if (it is ResponseException) {
          toast(it.response.info)
        } else {
          logg(it.stackTraceToString())
          toast("网络异常")
        }
      }
    }
  }

  @Composable
  fun NoBorderOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
  ) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
      colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
      value = value,
      modifier = if (label != null) {
        modifier
          // Merge semantics at the beginning of the modifier chain to ensure padding is
          // considered part of the text field.
          .semantics(mergeDescendants = true) {}
      } else {
        modifier
      }
        .background(colors.backgroundColor(enabled).value, shape)
        .defaultMinSize(
          minWidth = TextFieldDefaults.MinWidth,
          minHeight = TextFieldDefaults.MinHeight
        ),
      onValueChange = onValueChange,
      enabled = enabled,
      readOnly = readOnly,
      textStyle = mergedTextStyle,
      cursorBrush = SolidColor(colors.cursorColor(isError).value),
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      interactionSource = interactionSource,
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      decorationBox = @Composable { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
          value = value,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          placeholder = placeholder,
          label = label,
          leadingIcon = leadingIcon,
          trailingIcon = trailingIcon,
          singleLine = singleLine,
          enabled = enabled,
          isError = isError,
          interactionSource = interactionSource,
          colors = colors,
          border = {}
        )
      }
    )
  }

}