const params = new URLSearchParams(window.location.search);
const ATTEMPT_ID = params.get('attemptId');
const USE_CASE_PARAM = params.get('useCase') || 'default';
const DEEP_LINK = `tier1bank://verify?attemptId=${encodeURIComponent(ATTEMPT_ID)}`;

// Populate the UI
document.getElementById('sessionBadge').innerHTML = `Session ${ATTEMPT_ID.slice(0, 8)}&hellip;`;
document.getElementById('fallbackLink').href = DEEP_LINK;

// Set contextual message
const msgMap = {
  'login_2fa': "It's been 14 days since your last verified login.",
  'login': "It's been 14 days since your last verified login.",
  'transfer_auth': "Verification required for amounts greater than $1,000.",
  'account_recovery': "Please allow Chase to verify your device to continue password reset."
};
if (msgMap[USE_CASE_PARAM]) {
  document.getElementById('mainSub').textContent = msgMap[USE_CASE_PARAM];
}

const step = n => [1, 2, 3].forEach(i =>
  document.getElementById('s' + i).className = i < n ? 'done' : i === n ? 'active' : '');
const delay = ms => new Promise(r => setTimeout(r, ms));

// Common deep-link finish flow
async function finishFlowSuccess(phoneNumber) {
  // Mark success on our local server wrapper
  fetch('/verify/' + ATTEMPT_ID + '/complete', { method: 'POST' }).catch(() => { });

  step(3);
  document.getElementById('bar').style.width = '100%';
  document.getElementById('subStatus').innerHTML = `We have authorized your device over the cellular connection with the carrier and corroborated your phone number, <b>${phoneNumber}</b>, to your account. Verification complete!`;

  await delay(4000);
  window.location.href = DEEP_LINK;
  setTimeout(() => { document.getElementById('fallback').style.display = 'block'; }, 2000);
}

async function startVerification() {
  // Browsers require a user gesture (this click) to show the secure Glide popup.
  document.getElementById('formState').style.display = 'none';
  document.getElementById('loadingState').style.display = 'block';
  
  requestAnimationFrame(() => { document.getElementById('bar').style.width = '30%'; });
  document.getElementById('subStatus').textContent = 'Authenticating device...';

  try {
    const PLMN = params.get('plmn');
    if (!PLMN) {
      throw new Error("No PLMN was supplied via URL. Native Telephony extraction might have failed or phone is locked to WiFi-only.");
    }

    const { PhoneAuthClient, USE_CASE } = window.GlideWebClientSDK;
    const glideClient = new PhoneAuthClient({
      endpoints: {
        prepare: '/api/phone-auth/prepare',
        reportInvocation: '/api/phone-auth/report-invocation',
        process: '/api/phone-auth/process'
      }
    });

    // 1. Discover Phone Number
    const discoverResult = await glideClient.authenticate({
      use_case: 'GetPhoneNumber',
      plmn: {
        mcc: PLMN.substring(0, 3),
        mnc: PLMN.substring(3)
      }
    });

    if (!discoverResult.phone_number) {
      throw new Error("No phone number returned during discovery");
    }

    const phone = discoverResult.phone_number;

    step(2);
    document.getElementById('bar').style.width = '70%';
    document.getElementById('subStatus').textContent = 'Checking identity records...';

    await finishFlowSuccess(phone);
  } catch (error) {
    document.getElementById('subStatus').textContent = '❌ ' + (error.message || 'Error occurred during Glide auth');
    document.getElementById('subStatus').style.color = '#f85149';
    document.getElementById('bar').style.background = '#f85149';
  }
}
