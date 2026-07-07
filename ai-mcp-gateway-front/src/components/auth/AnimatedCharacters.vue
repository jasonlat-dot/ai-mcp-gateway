<template>
  <div class="auth-characters">
    <div class="characters-scene" ref="sceneRef">
        <!-- Purple character -->
        <div class="character char-purple" ref="purpleRef" id="char-purple">
          <div
            class="eyes"
            ref="purpleEyesRef"
            id="purple-eyes"
            :style="{ left: '45px', top: '40px', gap: '28px' }"
          >
            <div
              class="eyeball"
              ref="purpleEyeLRef"
              id="purple-eye-l"
              :style="{ width: '18px', height: '18px' }"
            >
              <div
                class="pupil"
                ref="purplePupilLRef"
                id="purple-pupil-l"
                :style="{ width: '7px', height: '7px' }"
              />
            </div>
            <div
              class="eyeball"
              ref="purpleEyeRRef"
              id="purple-eye-r"
              :style="{ width: '18px', height: '18px' }"
            >
              <div
                class="pupil"
                ref="purplePupilRRef"
                id="purple-pupil-r"
                :style="{ width: '7px', height: '7px' }"
              />
            </div>
          </div>
        </div>

        <!-- Black character -->
        <div class="character char-black" ref="blackRef" id="char-black">
          <div
            class="eyes"
            ref="blackEyesRef"
            id="black-eyes"
            :style="{ left: '26px', top: '32px', gap: '20px' }"
          >
            <div
              class="eyeball"
              ref="blackEyeLRef"
              id="black-eye-l"
              :style="{ width: '16px', height: '16px' }"
            >
              <div
                class="pupil"
                ref="blackPupilLRef"
                id="black-pupil-l"
                :style="{ width: '6px', height: '6px' }"
              />
            </div>
            <div
              class="eyeball"
              ref="blackEyeRRef"
              id="black-eye-r"
              :style="{ width: '16px', height: '16px' }"
            >
              <div
                class="pupil"
                ref="blackPupilRRef"
                id="black-pupil-r"
                :style="{ width: '6px', height: '6px' }"
              />
            </div>
          </div>
        </div>

        <!-- Orange character -->
        <div class="character char-orange" ref="orangeRef" id="char-orange">
          <div
            class="eyes"
            ref="orangeEyesRef"
            id="orange-eyes"
            :style="{ left: '82px', top: '90px', gap: '28px' }"
          >
            <div class="bare-pupil" ref="orangePupilLRef" id="orange-pupil-l" />
            <div class="bare-pupil" ref="orangePupilRRef" id="orange-pupil-r" />
          </div>
          <div
            class="orange-mouth"
            ref="orangeMouthRef"
            id="orange-mouth"
            :style="{ left: '90px', top: '120px' }"
          />
        </div>

        <!-- Yellow character -->
        <div class="character char-yellow" ref="yellowRef" id="char-yellow">
          <div
            class="eyes"
            ref="yellowEyesRef"
            id="yellow-eyes"
            :style="{ left: '52px', top: '40px', gap: '20px' }"
          >
            <div class="bare-pupil" ref="yellowPupilLRef" id="yellow-pupil-l" />
            <div class="bare-pupil" ref="yellowPupilRRef" id="yellow-pupil-r" />
          </div>
          <div
            class="yellow-mouth"
            ref="yellowMouthRef"
            id="yellow-mouth"
            :style="{ left: '40px', top: '88px' }"
          />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'

/**
 * 卡通角色眼睛动画组件
 * 原版位于 animatedlogin-main/index.html, 此处完全复刻 4 个角色(紫/黑/橙/黄)
 * 的形态与注视/回避/偷看/眨眼/摇头/偷看密码 等交互动画。
 *
 * 通过以下 prop 暴露场景状态:
 *  - typingUsername: 用户名输入框聚焦(角色互相对视)
 *  - typingPassword: 密码输入框聚焦
 *  - showPassword:   密码明文显示状态
 *  - passwordLength: 密码实际输入字符数
 *  - loginError:     登录失败(沮丧 + 摇头)
 *  - errorKey:       自增计数, 每次变化强制重启摇头动画
 *  - isMobile:       移动端直接隐藏
 */
const props = defineProps({
  typingUsername: { type: Boolean, default: false },
  typingPassword: { type: Boolean, default: false },
  showPassword:   { type: Boolean, default: false },
  passwordLength: { type: Number,  default: 0 },
  loginError:     { type: Boolean, default: false },
  errorKey:       { type: Number,  default: 0 },
})

/* ============ 元素引用 ============ */
const sceneRef = ref(null)

const purpleRef       = ref(null)
const blackRef        = ref(null)
const orangeRef       = ref(null)
const yellowRef       = ref(null)

const purpleEyesRef   = ref(null)
const purpleEyeLRef   = ref(null)
const purpleEyeRRef   = ref(null)
const purplePupilLRef = ref(null)
const purplePupilRRef = ref(null)

const blackEyesRef    = ref(null)
const blackEyeLRef    = ref(null)
const blackEyeRRef    = ref(null)
const blackPupilLRef  = ref(null)
const blackPupilRRef  = ref(null)

const orangeEyesRef   = ref(null)
const orangePupilLRef = ref(null)
const orangePupilRRef = ref(null)
const orangeMouthRef  = ref(null)

const yellowEyesRef   = ref(null)
const yellowPupilLRef = ref(null)
const yellowPupilRRef = ref(null)
const yellowMouthRef  = ref(null)

/* ============ 内部状态 ============ */
const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking  = ref(false)
const isPurplePeeking  = ref(false)

/* 角色之间短暂对视(800ms)的小状态机 */
const lookingAtEachOther = ref(false)
let typingEachOtherTimer = null

/* 派生场景状态 */
const isTypingUsername = computed(() => props.typingUsername)
const isLookingAway    = computed(() => props.typingPassword && !props.showPassword)
const isShowingPwd     = computed(() => props.passwordLength > 0 && props.showPassword)
const isLoginError     = computed(() => props.loginError)

/* 默认 mouse 位置 = 视口中心 */
function initMouseToCenter() {
  if (typeof window === 'undefined') return
  mouseX.value = window.innerWidth  / 2
  mouseY.value = window.innerHeight / 2
}

/* ============ 鼠标跟踪 ============ */
function handleMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  if (!isTypingUsername.value && !isLoginError.value) updateCharacters()
}

/* ============ 场景状态 -> 角色动画 的核心函数 ============ */

function calcPosition(el) {
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top  + rect.height / 3
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  const faceX   = Math.max(-15, Math.min(15, dx / 20))
  const faceY   = Math.max(-10, Math.min(10, dy / 30))
  const bodySkew = Math.max(-6, Math.min(6, -dx / 120))
  return { faceX, faceY, bodySkew }
}

function calcPupilOffset(el, maxDist) {
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top  + rect.height / 2
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  const dist = Math.min(Math.sqrt(dx * dx + dy * dy), maxDist)
  const angle = Math.atan2(dy, dx)
  return {
    x: Math.cos(angle) * dist,
    y: Math.sin(angle) * dist,
  }
}

function updateCharacters() {
  /* 元素未挂载完毕时直接退出 */
  if (!purpleRef.value || !blackRef.value || !orangeRef.value || !yellowRef.value) return

  const p = calcPosition(purpleRef.value)
  const b = calcPosition(blackRef.value)
  const o = calcPosition(orangeRef.value)
  const y = calcPosition(yellowRef.value)

  /* ============ Purple body ============ */
  if (isShowingPwd.value) {
    /* 显示密码时: 紫色根据 isPurplePeeking 微微前倾/回正, 增强偷看的实体感 */
    if (isPurplePeeking.value) {
      purpleRef.value.style.transform = 'skewX(-2deg) translateX(8px) translateY(0px)'
    } else {
      purpleRef.value.style.transform = 'skewX(0deg)'
    }
    purpleRef.value.style.height = '370px'
  } else if (isLookingAway.value) {
    /* 远离表单(向左倾 + 向左移) — 与原版一致 */
    purpleRef.value.style.transform = 'skewX(-14deg) translateX(-20px)'
    purpleRef.value.style.height = '410px'
  } else if (isTypingUsername.value) {
    /* 看向对方(左倾 + 右移, 紫色长高) — 与原版一致 */
    purpleRef.value.style.transform = `skewX(${(p.bodySkew || 0) - 12}deg) translateX(40px)`
    purpleRef.value.style.height = '410px'
  } else {
    purpleRef.value.style.transform = `skewX(${p.bodySkew}deg)`
    purpleRef.value.style.height = '370px'
  }

  /* ============ Purple eyes ============ */
  purpleEyeLRef.value.style.height = isPurpleBlinking.value ? '2px' : '18px'
  purpleEyeRRef.value.style.height = isPurpleBlinking.value ? '2px' : '18px'

  if (isLoginError.value) {
    /* Sad look down-left */
    purpleEyesRef.value.style.left = '30px'
    purpleEyesRef.value.style.top = '55px'
    purplePupilLRef.value.style.transform = 'translate(-3px, 4px)'
    purplePupilRRef.value.style.transform = 'translate(-3px, 4px)'
  } else if (isLookingAway.value) {
    /* Eyes look up-left (away from form) */
    purpleEyesRef.value.style.left = '20px'
    purpleEyesRef.value.style.top = '25px'
    purplePupilLRef.value.style.transform = 'translate(-5px, -5px)'
    purplePupilRRef.value.style.transform = 'translate(-5px, -5px)'
  } else if (isShowingPwd.value) {
    purpleEyesRef.value.style.left = '20px'
    purpleEyesRef.value.style.top = '35px'
    const px = isPurplePeeking.value ? 4 : -4
    const py = isPurplePeeking.value ? 5 : -4
    purplePupilLRef.value.style.transform = `translate(${px}px, ${py}px)`
    purplePupilRRef.value.style.transform = `translate(${px}px, ${py}px)`
  } else if (lookingAtEachOther.value) {
    purpleEyesRef.value.style.left = '55px'
    purpleEyesRef.value.style.top = '65px'
    purplePupilLRef.value.style.transform = 'translate(3px, 4px)'
    purplePupilRRef.value.style.transform = 'translate(3px, 4px)'
  } else {
    purpleEyesRef.value.style.left = `${45 + p.faceX}px`
    purpleEyesRef.value.style.top = `${40 + p.faceY}px`
    const po = calcPupilOffset(purpleEyeLRef.value, 5)
    purplePupilLRef.value.style.transform = `translate(${po.x}px, ${po.y}px)`
    purplePupilRRef.value.style.transform = `translate(${po.x}px, ${po.y}px)`
  }

  /* ============ Black body ============ */
  if (isShowingPwd.value) {
    blackRef.value.style.transform = 'skewX(0deg)'
  } else if (isLookingAway.value) {
    blackRef.value.style.transform = 'skewX(12deg) translateX(-10px)'
  } else if (lookingAtEachOther.value) {
    blackRef.value.style.transform =
      `skewX(${(b.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
  } else if (isTypingUsername.value) {
    blackRef.value.style.transform = `skewX(${(b.bodySkew || 0) * 1.5}deg)`
  } else {
    blackRef.value.style.transform = `skewX(${b.bodySkew}deg)`
  }

  /* ============ Black eyes ============ */
  blackEyeLRef.value.style.height = isBlackBlinking.value ? '2px' : '16px'
  blackEyeRRef.value.style.height = isBlackBlinking.value ? '2px' : '16px'

  if (isLoginError.value) {
    blackEyesRef.value.style.left = '15px'
    blackEyesRef.value.style.top = '40px'
    blackPupilLRef.value.style.transform = 'translate(-3px, 4px)'
    blackPupilRRef.value.style.transform = 'translate(-3px, 4px)'
  } else if (isLookingAway.value) {
    blackEyesRef.value.style.left = '10px'
    blackEyesRef.value.style.top = '20px'
    blackPupilLRef.value.style.transform = 'translate(-4px, -5px)'
    blackPupilRRef.value.style.transform = 'translate(-4px, -5px)'
  } else if (isShowingPwd.value) {
    blackEyesRef.value.style.left = '10px'
    blackEyesRef.value.style.top = '28px'
    blackPupilLRef.value.style.transform = 'translate(-4px, -4px)'
    blackPupilRRef.value.style.transform = 'translate(-4px, -4px)'
  } else if (lookingAtEachOther.value) {
    blackEyesRef.value.style.left = '32px'
    blackEyesRef.value.style.top = '12px'
    blackPupilLRef.value.style.transform = 'translate(0px, -4px)'
    blackPupilRRef.value.style.transform = 'translate(0px, -4px)'
  } else {
    blackEyesRef.value.style.left = `${26 + b.faceX}px`
    blackEyesRef.value.style.top = `${32 + b.faceY}px`
    const bo = calcPupilOffset(blackEyeLRef.value, 4)
    blackPupilLRef.value.style.transform = `translate(${bo.x}px, ${bo.y}px)`
    blackPupilRRef.value.style.transform = `translate(${bo.x}px, ${bo.y}px)`
  }

  /* ============ Orange body ============ */
  if (isLoginError.value) {
    orangeMouthRef.value.style.left = `${80 + o.faceX}px`
    orangeMouthRef.value.style.top = '130px'
  }
  if (isShowingPwd.value) {
    orangeRef.value.style.transform = 'skewX(0deg)'
  } else {
    orangeRef.value.style.transform = `skewX(${o.bodySkew}deg)`
  }

  /* ============ Orange eyes ============ */
  if (isLoginError.value) {
    orangeEyesRef.value.style.left = '60px'
    orangeEyesRef.value.style.top = '95px'
    orangePupilLRef.value.style.transform = 'translate(-3px, 4px)'
    orangePupilRRef.value.style.transform = 'translate(-3px, 4px)'
  } else if (isLookingAway.value) {
    orangeEyesRef.value.style.left = '50px'
    orangeEyesRef.value.style.top = '75px'
    orangePupilLRef.value.style.transform = 'translate(-5px, -5px)'
    orangePupilRRef.value.style.transform = 'translate(-5px, -5px)'
  } else if (isShowingPwd.value) {
    orangeEyesRef.value.style.left = '50px'
    orangeEyesRef.value.style.top = '85px'
    orangePupilLRef.value.style.transform = 'translate(-5px, -4px)'
    orangePupilRRef.value.style.transform = 'translate(-5px, -4px)'
  } else {
    orangeEyesRef.value.style.left = `${82 + o.faceX}px`
    orangeEyesRef.value.style.top = `${90 + o.faceY}px`
    const oo = calcPupilOffset(orangePupilLRef.value, 5)
    orangePupilLRef.value.style.transform = `translate(${oo.x}px, ${oo.y}px)`
    orangePupilRRef.value.style.transform = `translate(${oo.x}px, ${oo.y}px)`
  }

  /* ============ Yellow body ============ */
  if (isShowingPwd.value) {
    yellowRef.value.style.transform = 'skewX(0deg)'
  } else {
    yellowRef.value.style.transform = `skewX(${y.bodySkew}deg)`
  }

  /* ============ Yellow eyes & mouth ============ */
  if (isLoginError.value) {
    yellowEyesRef.value.style.left = '35px'
    yellowEyesRef.value.style.top = '45px'
    yellowPupilLRef.value.style.transform = 'translate(-3px, 4px)'
    yellowPupilRRef.value.style.transform = 'translate(-3px, 4px)'
    yellowMouthRef.value.style.left = '30px'
    yellowMouthRef.value.style.top = '92px'
    yellowMouthRef.value.style.transform = 'rotate(-8deg)'
  } else if (isLookingAway.value) {
    yellowEyesRef.value.style.left = '20px'
    yellowEyesRef.value.style.top = '30px'
    yellowPupilLRef.value.style.transform = 'translate(-5px, -5px)'
    yellowPupilRRef.value.style.transform = 'translate(-5px, -5px)'
    yellowMouthRef.value.style.left = '15px'
    yellowMouthRef.value.style.top = '78px'
    yellowMouthRef.value.style.transform = 'rotate(0deg)'
  } else if (isShowingPwd.value) {
    yellowEyesRef.value.style.left = '20px'
    yellowEyesRef.value.style.top = '35px'
    yellowPupilLRef.value.style.transform = 'translate(-5px, -4px)'
    yellowPupilRRef.value.style.transform = 'translate(-5px, -4px)'
    yellowMouthRef.value.style.left = '10px'
    yellowMouthRef.value.style.top = '88px'
    yellowMouthRef.value.style.transform = 'rotate(0deg)'
  } else {
    yellowEyesRef.value.style.left = `${52 + y.faceX}px`
    yellowEyesRef.value.style.top = `${40 + y.faceY}px`
    const yo = calcPupilOffset(yellowPupilLRef.value, 5)
    yellowPupilLRef.value.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    yellowPupilRRef.value.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    yellowMouthRef.value.style.left = `${40 + y.faceX}px`
    yellowMouthRef.value.style.top = `${88 + y.faceY}px`
    yellowMouthRef.value.style.transform = 'rotate(0deg)'
  }
}

/* ============ 眨眼定时器(随机 3-7 秒) ============ */
let purpleBlinkTimer = null
let blackBlinkTimer  = null

function startBlinkTimers() {
  stopBlinkTimers()
  scheduleBlinkPurple()
  scheduleBlinkBlack()
}

function stopBlinkTimers() {
  clearTimeout(purpleBlinkTimer)
  clearTimeout(blackBlinkTimer)
  purpleBlinkTimer = null
  blackBlinkTimer = null
}

function scheduleBlinkPurple() {
  purpleBlinkTimer = setTimeout(() => {
    isPurpleBlinking.value = true
    updateCharacters()
    setTimeout(() => {
      isPurpleBlinking.value = false
      updateCharacters()
      scheduleBlinkPurple()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

function scheduleBlinkBlack() {
  blackBlinkTimer = setTimeout(() => {
    isBlackBlinking.value = true
    updateCharacters()
    setTimeout(() => {
      isBlackBlinking.value = false
      updateCharacters()
      scheduleBlinkBlack()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

/* ============ 偷看定时器(密码可见时, 每 2-5 秒偷一次) ============ */
let peekTimer = null

function startPeekLoop() {
  stopPeekLoop()
  schedulePeek()
}

function stopPeekLoop() {
  clearTimeout(peekTimer)
  peekTimer = null
}

function schedulePeek() {
  if (!isShowingPwd.value) return
  peekTimer = setTimeout(() => {
    if (!isShowingPwd.value) return
    isPurplePeeking.value = true
    updateCharacters()
    setTimeout(() => {
      isPurplePeeking.value = false
      updateCharacters()
      schedulePeek()
    }, 800)
  }, Math.random() * 3000 + 2000)
}

/* ============ 角色互相偷看(对望 ~1200ms, 让视觉停留更明显) ============ */
function triggerLookingAtEachOther() {
  lookingAtEachOther.value = true
  clearTimeout(typingEachOtherTimer)
  typingEachOtherTimer = setTimeout(() => {
    lookingAtEachOther.value = false
    updateCharacters()
  }, 1200)
  updateCharacters()
}

/* ============ 摇头动画(失败) ============ */
let errorRecoveryTimer = null
const SHAKE_IDS = [
  'purple-eyes',
  'black-eyes',
  'orange-eyes',
  'yellow-eyes',
  'yellow-mouth',
  'orange-mouth',
]

function getShakeEls() {
  return SHAKE_IDS.map(id => document.getElementById(id)).filter(Boolean)
}

function triggerLoginError() {
  if (errorRecoveryTimer) {
    clearTimeout(errorRecoveryTimer)
    errorRecoveryTimer = null
  }

  /* 移除 class -> 强制 reflow -> 重新添加, 保证重复触发都能重新播放 */
  const shakeEls = getShakeEls()
  shakeEls.forEach(el => el.classList.remove('shake-head'))
  void document.body.offsetHeight

  /* 延时启动摇头, 等身体过渡 (0.7s) 完成约 1/3 — 让 shake 与身体倾斜同步 */
  setTimeout(() => {
    shakeEls.forEach(el => el.classList.add('shake-head'))
  }, 250)
}

/* ============ watch: 登录错误 -> 触发摇头 + 切换橙色嘴巴 ============ */
watch(() => props.errorKey, () => {
  nextTick(() => {
    triggerLoginError()
  })
})

watch(isLoginError, (v) => {
  if (orangeMouthRef.value) {
    orangeMouthRef.value.classList.toggle('visible', v)
  }
  updateCharacters()
})

/* ============ watch: 各个场景状态变化 ============ */
watch(isTypingUsername, (v) => {
  if (v) triggerLookingAtEachOther()
  else {
    clearTimeout(typingEachOtherTimer)
    lookingAtEachOther.value = false
  }
  updateCharacters()
})

watch([() => props.showPassword, () => props.passwordLength], () => {
  if (isShowingPwd.value) startPeekLoop()
  else {
    stopPeekLoop()
    isPurplePeeking.value = false
  }
  updateCharacters()
})

watch([isLookingAway, isLoginError], () => {
  updateCharacters()
})

/* ============ 生命周期 ============ */
onMounted(() => {
  initMouseToCenter()
  window.addEventListener('mousemove', handleMouseMove)
  startBlinkTimers()
  if (isShowingPwd.value) startPeekLoop()
  nextTick(() => updateCharacters())
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  stopBlinkTimers()
  stopPeekLoop()
  clearTimeout(typingEachOtherTimer)
  clearTimeout(errorRecoveryTimer)
})
</script>

<style scoped>
/* ============ 卡通角色场景 ============ */
.auth-characters {
  /* 渲染在 .brand-characters-wrap 内(relative), 角色底部对齐输入框区域中心,
     像原版那样"盯着"用户输入 */
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  pointer-events: none;
  user-select: none;
}

.characters-scene {
  /* 居中放置, 高度容纳最高角色(紫色 370px, 适当下沉 30px 让短角色"坐在"底部) */
  position: relative;
  width: 480px;
  height: 340px;
  margin: 0 auto;
}

/* 角色基类 */
.character {
  position: absolute;
  bottom: 0;
  /* 角色只在事件触发时变化(睁眼/睁大嘴/摇头),不需要永远过渡 */
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
  transform-origin: bottom center;
  will-change: transform;
  /* 浅色背景下,角色之间/与背景之间需要一点阴影做切分 */
  filter: drop-shadow(0 8px 16px rgba(99, 102, 241, 0.18));
}

/* 紫色矩形 */
.char-purple {
  left: 60px;
  width: 170px;
  height: 370px;
  background: #6c3ff5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}

/* 黑色矩形 */
.char-black {
  left: 220px;
  width: 115px;
  height: 290px;
  background: #2d2d2d;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

/* 橙色半圆 */
.char-orange {
  left: 0;
  width: 230px;
  height: 190px;
  background: #ff9b6b;
  border-radius: 115px 115px 0 0;
  z-index: 3;
}

/* 黄色圆角矩形 */
.char-yellow {
  left: 290px;
  width: 135px;
  height: 215px;
  background: #e8d754;
  border-radius: 68px 68px 0 0;
  z-index: 4;
}

/* ============ 眼睛 ============ */
.eyes {
  position: absolute;
  display: flex;
  /* 眼球位置移动 = 平滑追踪,但不要慢到产生拖尾感 */
  transition: left 0.35s ease-out, top 0.35s ease-out, gap 0.35s ease-out;
}

/* 眼球 */
.eyeball {
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: height 0.15s ease;
  overflow: hidden;
}

/* 瞳孔 */
.pupil {
  border-radius: 50%;
  background: #2d2d2d;
  transition: transform 0.1s ease-out;
}

/* 裸露瞳孔(无白眼球) */
.bare-pupil {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #2d2d2d;
  transition: transform 0.7s ease-in-out;
}

/* 黄色嘴巴 */
.yellow-mouth {
  position: absolute;
  width: 50px;
  height: 4px;
  background: #2d2d2d;
  border-radius: 2px;
  /* 形状/位置变化(咧开/紧闭)是离散事件,0.4s 足以,不卡顿 */
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
}

/* ============ 摇头动画 ============ */
@keyframes shakeHead {
  0%, 100% { translate: 0 0; }
  10%      { translate: -9px 0; }
  20%      { translate: 7px 0; }
  30%      { translate: -6px 0; }
  40%      { translate: 5px 0; }
  50%      { translate: -4px 0; }
  60%      { translate: 3px 0; }
  70%      { translate: -2px 0; }
  80%      { translate: 1px 0; }
  90%      { translate: -0.5px 0; }
}

.eyes.shake-head,
.yellow-mouth.shake-head,
.orange-mouth.shake-head {
  animation: shakeHead 0.8s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

/* 橙色悲伤嘴巴(错误时显示) */
.orange-mouth {
  position: absolute;
  width: 28px;
  height: 14px;
  border: 3px solid #2d2d2d;
  border-top: none;
  border-radius: 0 0 14px 14px;
  opacity: 0;
  transition: all 0.7s ease-in-out;
}

.orange-mouth.visible {
  opacity: 1;
}

/* 角色都基于各自 .char-* 容器定位, 这里不再做额外限制 */
</style>
