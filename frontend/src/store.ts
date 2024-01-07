import Vue from 'vue';
import Vuex from 'vuex';
import RemoteServices from '@/services/RemoteServices';
import TokenAuthUser from '@/models/user/TokenAuthUser';
import AuthUser from '@/models/user/AuthUser';
import RegisterUser from '@/models/user/RegisterUser';

interface State {
  token: string;
  user: AuthUser | null;
  error: boolean;
  errorMessage: string;
  notification: boolean;
  notificationMessageList: string[];
  loading: boolean;
}

const state: State = {
  token: '',
  user: null,
  error: false,
  errorMessage: '',
  notification: false,
  notificationMessageList: [],
  loading: false,
};

Vue.use(Vuex);
Vue.config.devtools = true;

export default new Vuex.Store({
  state: state,
  mutations: {
    initialiseStore(state) {
      const token = localStorage.getItem('token');
      if (token) {
        state.token = token;
      }
      const user = localStorage.getItem('user');
      if (user) {
        state.user = JSON.parse(user);
      }
    },
    login(state, authResponse: TokenAuthUser) {
      localStorage.setItem('token', authResponse.token);
      state.token = authResponse.token;
      localStorage.setItem('user', JSON.stringify(authResponse.user));
      state.user = authResponse.user;
    },
    logout(state) {
      localStorage.setItem('token', '');
      state.token = '';
      localStorage.setItem('user', '');
      state.user = null;
    },
    error(state, errorMessage: string) {
      state.error = true;
      state.errorMessage = errorMessage;
    },
    clearError(state) {
      state.error = false;
      state.errorMessage = '';
    },
    notification(state, notificationMessageList: string[]) {
      state.notification = true;
      state.notificationMessageList = notificationMessageList;
    },
    clearNotification(state) {
      state.notification = false;
      state.notificationMessageList = [];
    },
    loading(state) {
      state.loading = true;
    },
    clearLoading(state) {
      state.loading = false;
    },
  },
  actions: {
    error({ commit }, errorMessage) {
      commit('error', errorMessage);
    },
    clearError({ commit }) {
      commit('clearError');
    },
    notification({ commit }, message) {
      commit('notification', message);
    },
    clearNotification({ commit }) {
      commit('clearNotification');
    },
    loading({ commit }) {
      commit('loading');
    },
    clearLoading({ commit }) {
      commit('clearLoading');
    },
    async userLogin({ commit }, user: RegisterUser) {
      const authResponse = await RemoteServices.userLogin(
        user.username,
        user.password,
      );
      commit('login', authResponse);
    },
    async demoVolunteerLogin({ commit }) {
      const authResponse = await RemoteServices.demoVolunteerLogin();
      commit('login', authResponse);
    },
    async demoMemberLogin({ commit }) {
      const authResponse = await RemoteServices.demoMemberLogin();
      commit('login', authResponse);
    },
    async adminLogin({ commit }) {
      const authResponse = await RemoteServices.userLogin('ars', 'ars');
      commit('login', authResponse);
    },
    logout({ commit }) {
      return new Promise<void>((resolve) => {
        commit('logout');
        resolve();
      });
    },
  },
  getters: {
    isLoggedIn(state): boolean {
      return !!state.token;
    },
    isAdmin(state): boolean {
      return !!state.token && state.user !== null && state.user.role == 'ADMIN';
    },
    isMember(state): boolean {
      return (
        !!state.token && state.user !== null && state.user.role == 'MEMBER'
      );
    },
    isVolunteer(state): boolean {
      return (
        !!state.token && state.user !== null && state.user.role == 'VOLUNTEER'
      );
    },
    getToken(state): string {
      return state.token;
    },
    getUser(state): AuthUser | null {
      return state.user;
    },
    getError(state): boolean {
      return state.error;
    },
    getErrorMessage(state): string {
      return state.errorMessage;
    },
    getNotification(state): boolean {
      return state.notification;
    },
    getNotificationMessageList(state): string[] {
      return state.notificationMessageList;
    },
    getLoading(state): boolean {
      return state.loading;
    },
  },
});
