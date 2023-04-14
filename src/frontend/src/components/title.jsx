function Title(props) {

  return (
    <>
      <div className="h-[9vh] text-center font-bold text-[4vh]">
        {props.children}
      </div>
    </>
  )
}

export default Title;