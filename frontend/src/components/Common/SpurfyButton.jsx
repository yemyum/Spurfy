function SpurfyButton({
  children,
  variant = 'primary', // primary | danger | ai | outline | ghost | disabled
  className = '',
  ...props
}) {
  const base = 'rounded-lg transition font-semibold duration-200 border border-transparent';
  const variants = {
    primary: 'bg-[#9EC5FF] text-white hover:bg-[#7fb5ff]',
    danger: 'bg-[#575757] text-white hover:bg-[#444]',
    ai: 'bg-[#67F3EC] text-white hover:bg-[#42e3db]',
    outline: 'border border-[#9EC5FF] text-[#9EC5FF] hover:bg-[#e3f2ff]',
    ghost: 'text-[#9EC5FF] hover:underline',
    disabled: 'bg-[#9EC5FF]/50 text-white cursor-not-allowed',
  };

  return (
    <button
      className={`${base} ${variants[variant]} ${className}`}
      disabled={variant === 'disabled'}
      {...props}
    >
      {children}
    </button>
  );
}

export default SpurfyButton;