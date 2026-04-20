import { useTranslation } from '../i18n/I18nContext'

function RegisterHeader() {
  const { t } = useTranslation()
  return (
    <div className="text-center mb-4">
      <div className="d-inline-flex align-items-center gap-1 mb-1">
        <span className="fw-bold fs-4 font-monospace">{`{`}</span>
        <span className="fw-bold fs-5 font-monospace">EN</span>
        <span className="fw-bold fs-4 font-monospace">{`}`}</span>
      </div>
      <div className="fw-semibold mb-2">EventNode</div>
      <h1 className="fs-3 fw-bold">{t('auth.register')}</h1>
      <p className="text-primary small mt-1">
        {t('auth.registerSubtitle')}
      </p>
    </div>
  )
}

export default RegisterHeader
