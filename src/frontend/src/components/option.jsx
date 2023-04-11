function Option(props) {
  const item1 = ""
  const item2 = "text-right"

  return (
    <>
      <div className="flex gap-[2vw] mx-[15vw]">
        <div className="
          h-[5vh] w-[70vw] bg-[#2288BB] rounded-[5px] mb-[5vh] px-[2vw]
          text-white text-[3vh] grid grid-cols-2 gap-[2vw]
        ">
          <div className={item1}>{props.name}</div>
          <div className={item2}>{props.stock}</div>
        </div>
      </div>
    </>
  )
}

export default Option;