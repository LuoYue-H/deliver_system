import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  // 核心认证信息
  const token = ref(localStorage.getItem('token') || '');
  const role = ref(localStorage.getItem('role') || '');
  const userId = ref(localStorage.getItem('userId') || '');

  // 用户个人资料信息
  const username = ref(localStorage.getItem('username') || '');
  const avatar = ref(localStorage.getItem('avatar') || '');
  const merchantStatus = ref<number | null>(localStorage.getItem('merchantStatus') ? Number(localStorage.getItem('merchantStatus')) : null);

  function setLoginInfo(newToken: string, newRole: string, newUserId: string) {
    token.value = newToken;
    role.value = newRole;
    userId.value = newUserId;
    localStorage.setItem('token', newToken);
    localStorage.setItem('role', newRole);
    localStorage.setItem('userId', newUserId);
  }

  // 用于在刷新后或更新个人资料后，设置用户的详细信息
  function setUser(userProfile: any) {
    if (!userProfile) return;
    username.value = userProfile.username;
    avatar.value = userProfile.avatar;
    if (userProfile.merchantDetails) {
      merchantStatus.value = userProfile.merchantDetails.status;
      localStorage.setItem('merchantStatus', merchantStatus.value.toString());
    }
    // 注意：这里我们不修改 id 和 role，它们在登录时已确定
    // 如果需要，也可以一并更新
    localStorage.setItem('username', userProfile.username);
    localStorage.setItem('avatar', userProfile.avatar);
  }

  function logout() {
    token.value = '';
    role.value = '';
    userId.value = '';
    username.value = '';
    avatar.value = '';
    merchantStatus.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('avatar');
    localStorage.removeItem('merchantStatus');
  }

  return { token, role, userId, username, avatar, merchantStatus, setLoginInfo, setUser, logout };
});
