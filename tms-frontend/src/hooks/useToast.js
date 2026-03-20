/**
 * @file useToast.js
 * @description Custom React hook for managing toast notification state.
 * Provides a simple API to trigger  success or error messages
 * that appear in the bottom right corner of the screen and auto dismiss
 * after a set duration.
 *
 * @author Smriti Bajpai
 */

import { useState, useCallback } from 'react';


export const useToast = () => {

  /**
   * Toast display state.
   */
  const [toast, setToast] = useState({
    visible: false,
    message: '',
    isError: false,
  });

  /**
   * Triggers a toast notification with the given message and type.
   * Sets `visible` to true immediately to show the toast, then
   * schedules an auto-dismiss after 2800ms by setting `visible`
   */
  const showToast = useCallback((message, isError = false) => {

    /* Show the toast immediately with the provided message and style */
    setToast({ visible: true, message, isError });
    setTimeout(() => {
      setToast((prev) => ({ ...prev, visible: false }));
    }, 2800);

  }, []);

  return { toast, showToast };
};