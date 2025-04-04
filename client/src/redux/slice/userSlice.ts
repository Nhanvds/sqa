import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { UserInfo } from '../../types/user-info';

const initialState: { user: UserInfo | null} = { user: null };

const userSlice = createSlice({
    name: 'user',
    initialState,
    reducers: {
        setUser(state, action: PayloadAction<UserInfo>) {
            state.user = action.payload;
        },
        clearUser(state) {
            state.user = null;
        },
    },
});

export const { setUser, clearUser } = userSlice.actions;

export default userSlice.reducer;