import { useTranslation } from '../i18n/I18nContext'

function ForgotPasswordHeader() {
  const { t } = useTranslation()
  return (
    <div className="text-center mb-4">
      <h1 className="fs-3 fw-bold">{t('auth.recoverPassword')}</h1>
      <p className="text-secondary small mt-2">
        {t('auth.recoverInstructions')}
      </p>
    </div>
  )
}

export default ForgotPasswordHeader
