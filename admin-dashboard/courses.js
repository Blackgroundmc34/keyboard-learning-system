const API_BASE_URL=window.AdminAuth.apiBaseUrl;
const session = window.AdminAuth.requireAdmin();
const token = session?.token;
let courses = [];

const body = document.querySelector("#courses-body");
const search = document.querySelector("#course-search");
const statusFilter = document.querySelector("#status-filter");
const levelFilter = document.querySelector("#level-filter");

search.addEventListener("input", renderCourses);
window.addEventListener("admin-global-search", (event) => { search.value = event.detail; renderCourses(); });
statusFilter.addEventListener("change", renderCourses);
levelFilter.addEventListener("change", renderCourses);
document.querySelector("#add-course").addEventListener("click", openAddCourse);

loadCourses();

async function loadCourses() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/admin/courses`, { headers: { Authorization: `Bearer ${token}` } });
    if (response.status === 401 || response.status === 403) return window.AdminAuth.clearSession();
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to load courses");
    courses = result.courses;
    setText("course-total", result.summary.total);
    setText("course-published", result.summary.published);
    setText("course-drafts", result.summary.drafts);
    renderCourses();
  } catch (error) {
    showMessage(error.message || "Unable to connect to the backend server.");
    body.innerHTML = '<tr class="empty-row"><td colspan="8">Courses could not be loaded.</td></tr>';
  }
}

function renderCourses() {
  const term = search.value.trim().toLowerCase();
  const status = statusFilter.value;
  const level = levelFilter.value;
  const filtered = courses.filter((course) => {
    const matchesText = !term || `${course.title} ${course.description || ""} ${course.slug}`.toLowerCase().includes(term);
    return matchesText && (status === "ALL" || course.status === status) && (level === "ALL" || course.level === level);
  });

  body.innerHTML = filtered.length ? filtered.map(courseRow).join("") : '<tr class="empty-row"><td colspan="8">No courses match these filters.</td></tr>';
  document.querySelector("#course-result-count").textContent = `Showing ${filtered.length} of ${courses.length} courses`;
  body.querySelectorAll("[data-view]").forEach(button=>button.addEventListener("click",()=>viewCourse(courses.find(course=>course.id===Number(button.dataset.view)))));
  body.querySelectorAll("[data-edit]").forEach(button=>button.addEventListener("click",()=>openEditCourse(courses.find(course=>course.id===Number(button.dataset.edit)))));
  body.querySelectorAll("[data-delete]").forEach(button=>button.addEventListener("click",()=>confirmDeleteCourse(courses.find(course=>course.id===Number(button.dataset.delete)))));
}

function courseRow(course) {
  const image = course.thumbnailUrl ? `<img src="${escapeAttribute(course.thumbnailUrl)}" alt="" />` : "♫";
  const instructors = course.instructors.length ? course.instructors.join(", ") : "Not assigned";
  return `<tr>
    <td><div class="course-cell"><div class="course-thumb">${image}</div><span><strong>${escapeHtml(course.title)}</strong><small>${escapeHtml(course.description || "No description")}</small></span></div></td>
    <td class="instructor">${escapeHtml(instructors)}</td><td><span class="level">${formatLabel(course.level)}</span></td>
    <td>${course.lessonCount}</td><td>${course.enrollmentCount}</td>
    <td><span class="status ${course.status.toLowerCase()}">${formatLabel(course.status)}</span></td>
    <td>${formatDate(course.createdAt)}</td>
    <td><div class="row-actions"><button data-view="${course.id}" type="button" title="View course">◉</button><button data-edit="${course.id}" type="button" title="Edit course">✎</button><button data-delete="${course.id}" type="button" title="Delete course">⌫</button></div></td>
  </tr>`;
}

async function openAddCourse() {
  const instructors = await loadInstructors();

  window.AdminModal.openForm({
    title: "Add New Course",
    submitLabel: "Create Course",
    fields: courseFields(null,instructors),
    onSubmit: createCourse,
  });

  const titleInput = document.querySelector('#admin-modal-root [name="title"]');
  const slugInput = document.querySelector('#admin-modal-root [name="slug"]');
  let slugWasEdited = false;
  slugInput.addEventListener("input", () => { slugWasEdited = true; });
  titleInput.addEventListener("input", () => {
    if (!slugWasEdited) slugInput.value = toSlug(titleInput.value);
  });
}

async function openEditCourse(course) {
  const instructors=await loadInstructors();
  window.AdminModal.openForm({title:"Edit Course",submitLabel:"Save Changes",fields:courseFields(course,instructors),onSubmit:async data=>{
    await sendCourse(`/api/admin/courses/${course.id}`,"PATCH",data);await loadCourses();window.AdminModal.showSuccess({title:"Course Updated Successfully!",message:`“${data.title}” has been updated.`,primaryLabel:"Done"});
  }});
}

function viewCourse(course) { window.AdminModal.showSuccess({title:course.title,message:`${course.description || "No description"} • ${course.lessonCount} lessons • ${course.enrollmentCount} students • ${formatLabel(course.status)}`,primaryLabel:"Close",secondaryLabel:"Edit Course",onSecondary:()=>openEditCourse(course)}); }
function confirmDeleteCourse(course) { window.AdminModal.confirm({title:"Delete Course?",message:`This permanently deletes “${course.title}” and its lessons, materials, enrollments, and progress. This cannot be undone.`,confirmLabel:"Delete Course",onConfirm:async()=>{await sendCourse(`/api/admin/courses/${course.id}`,"DELETE");await loadCourses();showMessage(`“${course.title}” was deleted.`)}}); }
async function loadInstructors(){try{const response=await fetch(`${API_BASE_URL}/api/admin/resources/users`,{headers:{Authorization:`Bearer ${token}`}});const result=await response.json();return(result.records||[]).filter(user=>user.role==="INSTRUCTOR"&&user.isActive)}catch{return[]}}
function courseFields(course,instructors){return[
  {name:"title",label:"Course Title",placeholder:"Enter course title",required:true,value:course?.title},{name:"slug",label:"Slug",placeholder:"course-slug",required:true,hint:"Lowercase letters, numbers, and hyphens",value:course?.slug},
  {name:"level",label:"Level",type:"select",placeholder:"Select level",required:true,value:course?.level,options:[{value:"BEGINNER",label:"Beginner"},{value:"INTERMEDIATE",label:"Intermediate"},{value:"ADVANCED",label:"Advanced"},{value:"ALL_LEVELS",label:"All Levels"}]},
  {name:"instructorId",label:"Instructor",type:"select",placeholder:"No instructor assigned",value:course?.instructorIds?.[0]||"",options:instructors.map(user=>({value:String(user.id),label:user.name}))},
  {name:"description",label:"Description",type:"textarea",placeholder:"Describe what students will learn",maxlength:2000,rows:4,required:true,full:true,value:course?.description},
  {name:"thumbnailUrl",label:"Course Image URL",type:"url",placeholder:"https://example.com/course-image.jpg",full:true,value:course?.thumbnailUrl},
  {name:"estimatedDurationMinutes",label:"Duration (Minutes)",type:"number",min:"1",placeholder:"60",value:course?.estimatedDurationMinutes},
  {name:"isPublished",label:"Publish course",type:"checkbox",checked:course?.status==="PUBLISHED"}
]}
async function sendCourse(path,method,data){const response=await fetch(API_BASE_URL+path,{method,headers:{Authorization:`Bearer ${token}`,"Content-Type":"application/json"},body:data?JSON.stringify(data):undefined});const result=await response.json();if([401,403].includes(response.status))return window.AdminAuth.clearSession();if(!response.ok)throw new Error(result.message||"Unable to save course");return result}

async function createCourse(data) {
  const result = await sendCourse("/api/admin/courses","POST",data);
  await loadCourses();
  window.AdminModal.showSuccess({
    title: "Course Added Successfully!",
    message: `“${result.course.title}” has been added to your courses.`,
    primaryLabel: "Add Another Course",
    secondaryLabel: "View Course",
    onPrimary: openAddCourse,
    onSecondary: () => {
      search.value = result.course.title;
      statusFilter.value = "ALL";
      levelFilter.value = "ALL";
      renderCourses();
      document.querySelector(".course-panel").scrollIntoView({ behavior:"smooth" });
    },
  });
}

function showMessage(text) { const element=document.querySelector("#course-message"); element.textContent=text; element.classList.add("show"); window.setTimeout(()=>element.classList.remove("show"),3500); }
function setText(id,value) { document.querySelector(`#${id}`).textContent=Number(value).toLocaleString(); }
function formatLabel(value) { return value.toLowerCase().replaceAll("_"," ").replace(/\b\w/g,(letter)=>letter.toUpperCase()); }
function formatDate(value) { return new Intl.DateTimeFormat("en-ZA",{dateStyle:"medium"}).format(new Date(value)); }
function escapeHtml(value) { const element=document.createElement("span"); element.textContent=String(value); return element.innerHTML; }
function escapeAttribute(value) { return escapeHtml(value).replaceAll('"',"&quot;"); }
function toSlug(value) { return value.toLowerCase().trim().replace(/[^a-z0-9]+/g,"-").replace(/^-+|-+$/g,""); }
