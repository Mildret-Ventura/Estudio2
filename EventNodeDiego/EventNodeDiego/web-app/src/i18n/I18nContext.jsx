import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import es from './es.json'
import en from './en.json'

const translations = { es, en }
const I18nContext = createContext()

function getNestedValue(obj, path) {
  return path.split('.').reduce((acc, key) => acc?.[key], obj)
}

export function I18nProvider({ children }) {
  const [language, setLanguageState] = useState(() => {
    const stored = localStorage.getItem('eventnode-lang')
    if (stored && translations[stored]) return stored
    const browserLang = navigator.language?.slice(0, 2)
    return translations[browserLang] ? browserLang : 'es'
  })

  const setLanguage = useCallback((lang) => {
    if (translations[lang]) {
      setLanguageState(lang)
      localStorage.setItem('eventnode-lang', lang)
      document.documentElement.lang = lang
    }
  }, [])

  useEffect(() => {
    document.documentElement.lang = language
  }, [language])

  const t = useCallback((key, params = {}) => {
    let text = getNestedValue(translations[language], key) || getNestedValue(translations['es'], key) || key
    Object.entries(params).forEach(([k, v]) => {
      text = text.replace(new RegExp(`\\{\\{${k}\\}\\}`, 'g'), v)
    })
    return text
  }, [language])

  return (
    <I18nContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </I18nContext.Provider>
  )
}

export function useI18n() {
  const context = useContext(I18nContext)
  if (!context) throw new Error('useI18n must be used within I18nProvider')
  return context
}

export const useTranslation = useI18n
